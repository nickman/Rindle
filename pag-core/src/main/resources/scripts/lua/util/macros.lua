local macros = {}

macros.getMetrics = function(...) 
	local values = {}
	for i=1, #arg do
	    local id = tostring(arg[i]);
	    local val = redis.call('HGETALL', id)
      rlog.info("Fetching metric:" .. id)
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

macros.getMetricsJson = function(...) 
  return cjson.encode(macros.getMetrics(arg))
end



macros.fireNewMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.METRIC.NEW', macros.getMetricsJson(gid))
end

macros.fireUpdatedMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.METRIC.UPDATE', macros.getMetricsJson(gid))
end


macros.getIdsForPattern = function(pattern, count)
  if(count==nil) then count = 10 end
  local cursor = '0'
  local scanResult = nil
  local results = {}
  repeat    
    scanResult = redis.call('SCAN', cursor, 'MATCH', pattern, 'COUNT', count)    
    cursor = table.remove(scanResult, 1)
    for i=1, #scanResult do
      for x=1, #scanResult[i] do
        results[#results+1] = redis.call('GET', scanResult[i][x])
      end
    end
  until cursor == '0'
  rlog.debug('getIdsForPattern.results: --> ' .. cjson.encode(results))
  return results
end

macros.invokeIdsForPattern = function()  
  return macros.getIdsForPattern(ARGV[1], ARGV[2])
end

--[[
macros.processTree = {
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

macros.nvl = function(value) 
  if(value==nil or value=='NULL') then
    return '0'
  else
    return '1'
  end
end

macros.maskArgs = function(name, nameId, key, keyId) 
  return macros.nvl(name) .. macros.nvl(nameId) .. macros.nvl(key) .. macros.nvl(keyId)
end

macros.process = function(name, nameId, key, keyId, timestamp)
    local mask = macros.maskArgs(name, nameId, key, keyId)
    rlog.info("Executing processTree [" .. mask .. "]")
    local fx = macros.processTree[mask]
    if(fx ~= nil) then
      return fx(name, nameId, key, keyId, timestamp)
    end
end

macros.processTree = {
	['1000']		 	--[[ only name ]] = function(name, nameId, key, keyId, timestamp) 
		-- Create a new GID. Save the name->ID key, Save the GID hash, publish new metric
		local gid = redis.call('incr','gidcounter')
    redis.call('hmset', gid, 'n', name, 'ts', timestamp)
    redis.call('set', name, gid)
    macros.fireNewMetric(gid)
    return gid
	end,
	['0010']		 	--[[ only key ]] = function(name, nameId, key, keyId, timestamp) 
		-- Create a new GID. Save the key->ID key, Save the GID hash, publish new metric
		local gid = redis.call('incr','gidcounter')
    redis.call('hmset', gid, 'o', key, 'ts', timestamp)
    redis.call('set', key, gid)
    macros.fireNewMetric(gid)
    return gid
	end,
  
	['1010']		 	--[[ only name and key ]] = function(name, nameId, key, keyId, timestamp) 
    -- Create a new GID. Save the name->ID key, Save the key->ID, Save the GID hash, publish new metric
		local gid = redis.call('incr','gidcounter')
    redis.call('hmset', gid, 'n', name, 'o', key, 'ts', timestamp)
    redis.call('mset', name, gid, key, gid)
    macros.fireNewMetric(gid)	
    return gid
	end, 
	['1011']		 	--[[ name only, key and keyId ]] = function(name, nameId, key, keyId, timestamp) 
    -- Save the name->ID key, Add name to hash, publish updated metric
    redis.call('set', name, keyId)
    redis.call('hset', keyId, 'n', name)
    macros.fireUpdatedMetric(keyId)
    return keyId
	end,
	['1110']		 	--[[ name and nameId, key but no keyId ]]  = function(name, nameId, key, keyId, timestamp) 
    -- Save the key->ID key, Add key to hash, publish updated metric
    redis.call('set', key, nameId)
    redis.call('hset', nameId, 'o', key)
    macros.fireUpdatedMetric(nameId)
    return nameId
	end,
  --  ======================== NO OPs =========================
  ['0000'] = function(name, nameId, key, keyId, timestamp)
    return nil
   end,
  ['1100'] = function(name, nameId, key, keyId, timestamp)
    return nameId
   end,
  ['0011'] = function(name, nameId, key, keyId, timestamp)
    return keyId
   end,
  ['1111'] = function(name, nameId, key, keyId, timestamp)
    return nameId
   end   
}


rawset(_G, "macros", macros)


