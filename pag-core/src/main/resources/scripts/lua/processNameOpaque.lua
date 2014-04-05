local metricName = KEYS[1]
local opaqueKey = KEYS[2]
local gid = redis.call('incr','gidcounter')
redis.call('hmset', gid, 'N', metricName, 'O', opaqueKey)
redis.call('set', opaqueKey, gid)
redis.call('set', metricName, gid);
return gid;

