local session = {}

session.SPREFIX = 'S:'
-- =========================================================
--   Session Keys
-- =========================================================
session.SGIDFMT = 'SG:'
session.PGIDFMT = 'PG:'
session.PTFMT = 'PTS:'
session.SUBSFMT = 'SUB:'
-- =========================================================
session.EXPIRATION = 300 
session.STOREKEY = 'rindlesessionstore'
session.GIDSUBTRACKER = 'rindlegidsubtracker'

session.sprefix = function(name) 
  if(name==nil or name=='NULL') then return nil end
  if(string.find(name, session.SPREFIX)==1) then return name end
  return session.SPREFIX .. name
end


session.session = function(Id)
  if(Id == nil) then
    return redis.error_reply('Session ID cannot be nil')
  end
  local sessionId = session.sprefix(Id)
   
  if(redis.call('EXISTS', sessionId)==0) then
    local specGids = session.SGIDFMT .. Id
    local patGids = session.PGIDFMT .. Id
    local patterns = session.PTFMT .. Id
    local subs = session.SUBSFMT .. Id
    redis.call('HMSET', sessionId,
      'sgids', specGids, 
      'pgids', patGids, 
      'patterns', patterns, 
      'subs', subs
    )
    redis.call('SADD', specGids, -1) redis.call('SREM', specGids, -1) 
    redis.call('SADD', patGids, -1) redis.call('SREM', patGids, -1)
    redis.call('SADD', patterns, '-1') redis.call('SREM', patterns, '-1')
    redis.call('SADD', subs, '[]')
    redis.call('SADD', session.STOREKEY, sessionId)    
  end
  redis.call('EXPIRE', sessionId, session.EXPIRATION)
  return sessionId
end

session.processGlobalId = function(Id, globalId, addOp, specified) 
  if(globalId == nil or rindle.metricExists(globalId)==false) then
    return redis.error_reply('Invalid global id:' .. globalId)
  end
  local sessionId = session.session(Id)  
  local op = (addOp and 'SADD' or 'SREM')
  local sessKey = (specified and (session.SGIDFMT .. Id) or (session.PGIDFMT .. Id))
  if(redis.call(op, sessKey, globalId)==1) then
    local zresult = redis.call('ZINCRBY', session.GIDSUBTRACKER, (addOp and 1 or -1), globalId)
    if(zresult==1) then
      redis.call('PUBLISH', 'RINDLE.SUBSCRIPTION.STARTED', rindle.getMetricsJson(globalId))  
    elseif(zresult==-1) then
      redis.call('PUBLISH', 'RINDLE.SUBSCRIPTION.ENDED', rindle.getMetricsJson(globalId))
    end        
  end
end

session.invoke = function() 
  local functionName = table.remove(ARGV, 1)
  local paramOne = table.remove(ARGV, 1)
  session[functionName](paramOne, ARGV)
end;

session.addSpecifiedGlobalId = function(Id, ...)
  for i=1, #arg do
    session.processGlobalId(Id, arg[i], true, true)
  end
end

session.removeSpecifiedGlobalId = function(Id, ...)
  for i=1, #arg do
    session.processGlobalId(Id, arg[i], false, true)
  end
end

session.addPatternMatchGlobalId = function(Id, ...)
  for i=1, #arg do
    session.processGlobalId(Id, arg[i], true, false)
  end
end

session.removePatternMatchGlobalId = function(Id, ...)
  for i=1, #arg do
    session.processGlobalId(Id, arg[i], false, false)
  end
end

session.addPattern = function(Id, ...)
  local sessionId = session.session(Id)
  local patKey = session.PTFMT .. Id
  local added = 0
  for i=1, #arg do
    added = added + redis.call('SADD', patKey, arg[i])
  end
  return added
end

session.removePattern = function(Id, ...)
  local sessionId = session.session(Id)
  local patKey = session.PTFMT .. Id
  local removed = 0
  for i=1, #arg do
    removed = removed + redis.call('SREM', patKey, arg[i])
  end
  return removed
end
















rawset(_G, "session", session)