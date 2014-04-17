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
	    	end;
	    	values[#values+1] = entry;
	    end    
	end
	rlog.debug("returning " .. cjson.encode(values))
	return values
end;

macros.getMetricsJson = function(...) 
  return cjson.encode(macros.getMetrics(arg))
end


macros.getIdsForPattern = function(pattern)
	local keys = redis.call('KEYS', pattern)
	rlog.info("keys type:" .. type(keys))
	return redis.call('MGET', keys)
end;


rawset(_G, "macros", macros)


