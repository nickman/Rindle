redis.call("SELECT", "1")
rlog.info("Calling processNameOpaque[" .. KEYS[1] .. ", " .. KEYS[2] .. "]")
local metricName = KEYS[1]
local opaqueKey = KEYS[2]

if(metricName == 'NULL' and opaqueKey == 'NULL') then 
	return -1
end

local mnId = (metricName ~= 'NULL' and redis.call('get',metricName) or nil)
local okId = (opaqueKey ~= 'NULL' and redis.call('get', opaqueKey) or nil)

local gid = nil

if(mnId == nil) then
	rlog.info('MnId Type: nil')
else 
	rlog.info('MnId Type:' .. type(mnId) .. ' v:[' .. mnId .. ']')
	return mnId
end

rlog.info('Keys: MnId:[' , mnId , ']')
rlog.info('Keys: OkId:[', okId, ']')






if(mnId ~= nil and okId ~= nil) then 
	rlog.info('Neither null')
	if(mnId ~= okId) then
		rlog.warn('Metric GID Mismatch Name:' , mnId , 'Opaque:' , okId)
		return {mnId, okId};
	end
	return mnId
end


rlog.info('============== Processing inserts ==================')

if(mnId == nil and okId == nil) then 
	gid = redis.call('incr','gidcounter')
else
	gid = ((mnId ~= nil and mnId) or okId)
end

rlog.debug('One null: MnId:', mnId, ' OkId:', okId, 'GID:', gid)

if(metricName ~= nil and opaqueKey ~= nil) then
	-- Both new
	redis.call('hmset', gid, 'N', metricName, 'O', opaqueKey)
	redis.call('set', opaqueKey, gid)
	redis.call('set', metricName, gid)
elseif(metricName ~= nil) then 
	-- metric name was not null
	if(mnId ~= nil) then
		-- metric was assigned, opaque was not. Nothing to do
		rlog.debug('Metric Name Already Assigned: [', metricName, ']:', mnId) 		
	else 
		-- metric was not assigned		
		redis.call('hmset', gid, 'N', metricName)
		redis.call('set', metricName, gid)
		rlog.debug('Metric Name New: [', metricName, ']:', mnId) 		
	end
elseif(opaqueKey ~= nil) then 
	-- opaque key was not null
	if(okId ~= nil) then
		-- opaque key was assigned, metric name was not. Nothing to do
		rlog.debug('Opaque Key Already Assigned: [', opaqueKey, ']:', okId) 		
	else 
		-- opaque key was not assigned		
		redis.call('hmset', gid, 'O', opaqueKey)
		redis.call('set', opaqueKey, gid)
		rlog.debug('Opaque Key New: [', opaqueKey, ']:', okId) 		
	end
end


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

rlog.info("Loaded processNameOpaque")
return gid



