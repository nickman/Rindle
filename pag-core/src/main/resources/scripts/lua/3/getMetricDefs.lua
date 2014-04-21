redis.call("SELECT", rindle.db)
rlog.debug("Calling getMetricDefs[", KEYS, "]")
local values = {}

for i=1, #KEYS do
  local KEY = rindle.gprefix(KEYS[i])
  local val = redis.call('HGETALL', KEY)
  local entry = {};
  entry['id'] = tonumber(KEYS[i]);
  local k = nil;
  local v = nil;

  if(val ~= nil) then
    for key, val in pairs(val) do
      if(key%2==1) then
        k = val;
      else
        v = val;
        entry[k] = v;
      end
    end;
    --values[KEYS[i]] = entry;
    values[#values+1] = entry;
  end
end
return cjson.encode(values);

--	// from REDIS: {"3":["O","XYX"],"4":["N","SNA","O","FU"]}
--	// from Jackson:  {"id":54,"ts":1397602986240,"n":"FooBar","o":"Um05dlFtRnk="}
--	// [
--		{"id":54,"ts":1397610322489,"n":"FooBar","o":"Um05dlFtRnk="},
--		{"id":77,"ts":1397610322489,"n":"MeToo","o":"V1c5NWJ3PT0A"}
--		]

