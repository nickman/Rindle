local macros = {}

macros.getMetrics = function(...) 
	local values = {}
	for i=1, #arg do
	    local id = tostring(arg[i]);
	    rlog.debug("getting:" .. id .. ', type:' .. type(id))
	    local val = redis.call('HGETALL', id)
	    local entry = {};
	    entry['id'] = id;
		local k = nil;
	    if(val ~= nil) then
	    	rlog.trace("processing " .. cjson.encode(val))
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
	rlog.debug("returning " .. cjson.encode(values))
	return values
end

macros.getMetricsJson = function(...) 
  return cjson.encode(macros.getMetrics(arg))
end


macros.getIdsForPattern = function(pattern)
	local keys = redis.call('KEYS', pattern)	
	return redis.call('MGET', unpack(keys))
end;

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

rawset(_G, "macros", macros)


