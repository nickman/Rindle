local metricName = KEYS[1]
local opaqueKey = KEYS[2]
redis.call('PUBLISH', 'RINDLELOG', 'Fooooo');
if(metricName == nil and opaqueKey == nil) then 
	return -1
end

local mnId = (metricName ~= nil and orredis.call('get',metricName) or nil)
local okId = (opaqueKey ~= nil and redis.call('get', opaqueKey) or nil)

redis.call('PUBLISH', 'RINDLELOG', 'MnId:'..mnId..'OkId:'..okId)

local gid = redis.call('incr','gidcounter')

if(metricName ~= nil and opaqueKey ~= nil) then
	redis.call('hmset', gid, 'N', metricName, 'O', opaqueKey)
	redis.call('set', opaqueKey, gid)
	redis.call('set', metricName, gid);
elseif(metricName ~= nil) then 
	redis.call('hmset', gid, 'N', metricName)
	redis.call('set', metricName, gid);
elseif(opaqueKey ~= nil) then 
	redis.call('hmset', gid, 'O', opaqueKey)
	redis.call('set', opaqueKey, gid)
end


return gid;



--[[    =========================
	 * Lua Master Processor
	 * ====================
	 * KEYS: metricName, opaqueKey
	 * 	if(metricName!=null) mid = get(metricName)
	 * 	if(opaqueKey!=null) oid = get(opaqueKey)
	 * 
	 * 	Conditions:
	 * 		both null: error
	 * 		one null:
	 * 			id found: return
	 * 			else save, return new id
	 *  	neither null:
	 *  		both ids found:
	 *  			ids equal: return id
	 *  			ids diff: <???>
	 *  		neither id found:  save both under new id and return new id
	 *  		one id found:
	 *  			update for missing, return id


]]
