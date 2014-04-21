local rindle = {}

rindle.db = 1
rindle.GPREFIX = 'G:'
rindle.NPREFIX = 'N:'
rindle.OPREFIX = 'O:'


rindle.nprefix = function(name) 
  if(name==nil or name=='NULL') then return nil end
  if(string.find(name, rindle.NPREFIX)==1) then return name end
  return rindle.NPREFIX .. name
end

rindle.oprefix = function(okey) 
  if(okey==nil or okey=='NULL') then return nil end
  if(string.find(okey, rindle.OPREFIX)==1) then return okey end
  return rindle.OPREFIX .. okey
end

rindle.gprefix = function(gid) 
  if(gid==nil) then return nil end
  if(type(gid) ~= 'string') then gid = tostring(gid) end
  if(string.find(gid, rindle.GPREFIX)==1) then return gid end
  return rindle.GPREFIX .. gid
end

rindle.gunfix = function(gid) 
  if(gid==nil) then return nil end
  if(type(gid) ~= 'string') then return gid end  
  if(string.find(gid, rindle.GPREFIX)~=1) then return gid end
  return string.sub(gid, 3)
end

rindle.metricExists = function(globalId) 
  if(globalId == nil) then return false end
  return redis.call('EXISTS', rindle.gprefix(globalId))
end

rindle.getMetrics = function(...)
  
	local values = {}
	for i=1, #arg do
	    local id = tostring(arg[i]);
	    local val = redis.call('HGETALL', 'G:' .. id)
	    local entry = {};
	    entry['id'] = id;
      local k = nil;
	    if(val ~= nil) then
	    	for key=1, #val do
	    		if(key%2==1) then
					k = val[key];    		
	    		else 
	    			entry[k] = val[key];
	    		end
	    	end
	    	values[#values+1] = entry;
	    end    
	end
	return values
end

rindle.getMetricsJson = function(...)
  return cjson.encode(rindle.getMetrics(unpack(arg)))
end



rindle.fireNewMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.METRIC.NEW', rindle.getMetricsJson(rindle.gunfix(gid)))
end

rindle.fireUpdatedMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.METRIC.UPDATE', rindle.getMetricsJson(rindle.gunfix(gid)))
end


rindle.getIdsForPattern = function(pattern, count)
  if(count==nil) then count = 10 end
  local cursor = '0'
  local scanResult = nil
  local results = {}
  repeat    
    scanResult = redis.call('SCAN', cursor, 'MATCH', rindle.nprefix(pattern), 'COUNT', count)    
    cursor = table.remove(scanResult, 1)
    for i=1, #scanResult do
      for x=1, #scanResult[i] do        
        results[#results+1] = rindle.gunfix(redis.call('GET', scanResult[i][x]))
        
      end
    end
  until cursor == '0'
  rlog.debug('getIdsForPattern.results: --> ' .. cjson.encode(results))
  return results
end

rindle.invokeIdsForPattern = function()  
  rlog.debug('getIdsForPattern(' .. ARGV[1] .. ')')
  return rindle.getIdsForPattern(ARGV[1], ARGV[2])
end

--[[
rindle.processTree = {
	{0,0,0,0}, 	-- nothing found (NO OP)
	{1,0,0,0}, 	-- only name
	{1,0,1,0}, 	-- only name and key 
	{1,0,1,1}, 	-- name only, key and keyId
	{1,1,0,0}, 	-- name and nameId (NO OP)
	{1,1,1,0}, 	-- name and nameId, key but no keyId
	{1,1,1,1}, 	-- name, key, nameId and key id all found (NO OP)
	{0,0,1,1} 	-- key and keyId (NO OP)
}
]]

rindle.nvl = function(value) 
  if(value==nil or value=='NULL') then
    return '0'
  else
    return '1'
  end
end

rindle.maskArgs = function(name, nameId, key, keyId) 
  return rindle.nvl(name) .. rindle.nvl(nameId) .. rindle.nvl(key) .. rindle.nvl(keyId)
end

rindle.process = function(name, nameId, key, keyId, timestamp)
    local mask = rindle.maskArgs(name, nameId, key, keyId)
    rlog.debug("Executing processTree [" .. mask .. "]")
    local fx = rindle.processTree[mask]
    if(fx ~= nil) then
      return fx(name, nameId, key, keyId, timestamp)
    end
end

rindle.nextGid = function() 
  return rindle.gprefix(redis.call('incr','gidcounter'))
end

rindle.processTree = {
	['1000']		 	--[[ only name ]] = function(name, nameId, key, keyId, timestamp) 
		-- Create a new GID. Save the name->ID key, Save the GID hash, publish new metric
		local gid = rindle.nextGid()
    redis.call('hmset', gid, 'n', name, 'ts', timestamp)
    redis.call('set', name, gid)
    rindle.fireNewMetric(gid)
    return rindle.gunfix(gid)
	end,
	['0010']		 	--[[ only key ]] = function(name, nameId, key, keyId, timestamp) 
		-- Create a new GID. Save the key->ID key, Save the GID hash, publish new metric
		local gid = rindle.nextGid()
    redis.call('hmset', gid, 'o', key, 'ts', timestamp)
    redis.call('set', key, gid)
    rindle.fireNewMetric(gid)
    return rindle.gunfix(gid)
	end,
  
	['1010']		 	--[[ only name and key ]] = function(name, nameId, key, keyId, timestamp) 
    -- Create a new GID. Save the name->ID key, Save the key->ID, Save the GID hash, publish new metric
		local gid = rindle.nextGid()
    redis.call('hmset', gid, 'n', name, 'o', key, 'ts', timestamp)
    redis.call('mset', name, gid, key, gid)
    rindle.fireNewMetric(gid)	
    return rindle.gunfix(gid)
	end, 
	['1011']		 	--[[ name only, key and keyId ]] = function(name, nameId, key, keyId, timestamp) 
    -- Save the name->ID key, Add name to hash, publish updated metric
    redis.call('set', name, keyId)
    redis.call('hset', keyId, 'n', name)
    rindle.fireUpdatedMetric(keyId)
    return rindle.gunfix(keyId)
	end,
	['1110']		 	--[[ name and nameId, key but no keyId ]]  = function(name, nameId, key, keyId, timestamp) 
    -- Save the key->ID key, Add key to hash, publish updated metric
    redis.call('set', key, nameId)
    redis.call('hset', nameId, 'o', key)
    rindle.fireUpdatedMetric(nameId)
    return rindle.gunfix(nameId)
	end,
  --  ======================== NO OPs =========================
  ['0000'] = function(name, nameId, key, keyId, timestamp)
    return -1
   end,
  ['1100'] = function(name, nameId, key, keyId, timestamp)
    return rindle.gunfix(nameId)
   end,
  ['0011'] = function(name, nameId, key, keyId, timestamp)
    return rindle.gunfix(keyId)
   end,
  ['1111'] = function(name, nameId, key, keyId, timestamp)
    return rindle.gunfix(nameId)
   end   
}


rawset(_G, "rindle", rindle)


