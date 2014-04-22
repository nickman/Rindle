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
session.PATSUBTRACKER = 'rindlepatternsubtracker'

session.sprefix = function(name) 
  if(name==nil or name=='NULL') then return nil end
  if(string.find(name, session.SPREFIX)==1) then return name end
  return session.SPREFIX .. name
end


session.session = function()
  if(ARGV[1] == nil) then
    return redis.error_reply('Session ID cannot be nil')
  end
  local Id = ARGV[1] 
  local sessionId = session.sprefix(Id)
  local specGids = session.SGIDFMT .. Id
  local patGids = session.PGIDFMT .. Id
  local patterns = session.PTFMT .. Id
  local subs = session.SUBSFMT .. Id
   
  if(redis.call('EXISTS', sessionId)==0) then
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
  redis.call('EXPIRE', specGids, session.EXPIRATION)
  redis.call('EXPIRE', patGids, session.EXPIRATION)
  redis.call('EXPIRE', patterns, session.EXPIRATION)
  redis.call('EXPIRE', subs, session.EXPIRATION)
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
  local fx = session[functionName];
  return session[functionName](paramOne, unpack(ARGV))
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
    local pattern = arg[1]
    added = added + redis.call('SADD', patKey, pattern)  
    local zresult = redis.call('ZINCRBY', session.PATSUBTRACKER, 1, pattern)
    if(zresult==1) then
      rlog.info('Pattern Started: [' .. pattern .. ']')
      redis.call('PUBLISH', 'RINDLE.PATTERN.STARTED', pattern)  
    end         
  end
  return added
end

session.removePattern = function(Id, ...)
  if(Id==nil) then Id = ARGV[1] end
  if(arg==nil) then 
    table.remove(ARGV, 1)
    arg = ARGV[1] 
  end
  local sessionId = session.session(Id)
  local patKey = session.PTFMT .. Id
  local removed = 0
  for i=1, #arg do
    local pattern = arg[1]
    removed = removed + redis.call('SREM', patKey, pattern)
    local zresult = redis.call('ZINCRBY', session.PATSUBTRACKER, -1, pattern)
    if(zresult==0) then
      rlog.info('Pattern Started: [' .. pattern .. ']')
      redis.call('PUBLISH', 'RINDLE.PATTERN.ENDED', pattern)  
    end         
    
  end
  return removed
end

session.getSessionData = function(key) 
  if(key==nil) then key = ARGV[1] end
  local ids = {}
  if(redis.call('EXISTS', key)==1) then
    for i,v in pairs(redis.call('SMEMBERS', key)) do
      table.insert(ids, v)
    end
  end;
  return ids;
end


session.getSpecifiedGlobalIds = function(Id)
  if(Id==nil) then Id = ARGV[1] end
  return session.getSessionData(session.SGIDFMT .. Id)
end

session.getPatternMatchGlobalIds = function(Id)
  if(Id==nil) then Id = ARGV[1] end
  return session.getSessionData(session.PGIDFMT .. Id)
end

session.getPatterns = function(Id)
  if(Id==nil) then Id = ARGV[1] end
  return session.getSessionData(session.PTFMT .. Id)
end


session.terminate = function(Id)
  if(Id==nil) then Id = ARGV[1] end
  redis.call('DEL', session.sprefix(Id))
  redis.call('DEL', session.SGIDFMT .. Id)
  redis.call('DEL', session.PGIDFMT .. Id)
  redis.call('DEL', session.PTFMT .. Id)
  redis.call('DEL', session.SUBSFMT .. Id)
  redis.call('SREM', session.STOREKEY, session.sprefix(Id))
end

session.ttl = function(Id)
  if(Id==nil) then Id = ARGV[1] end
  local ttl = redis.call('TTL', session.sprefix(Id))
  rlog.info("Session:[" .. session.sprefix(Id) .. "]:" .. ttl)
  return tostring(ttl)
end










rawset(_G, "session", session)