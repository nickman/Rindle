redis.call("SELECT", "1")
rlog.info("Calling getMetricDefs[", KEYS, "]")
local values = ''
for i=1, #KEYS do 
	rlog.info("Fetching:" .. KEYS[i])
    local val = redis.call('HGETALL', KEYS[i])
    --rlog.info("Pushing:" .. cjson.encode(val))
    --rlog.info("Pushing:" , val)
    if(val ~= nil) then
    	rlog.info("Pushing:" , cjson.encode(values))
		values = values .. cjson.encode(values);
		
    end    
end
--rlog.info('JSON:' .. cjson.encode(values))
return values;

