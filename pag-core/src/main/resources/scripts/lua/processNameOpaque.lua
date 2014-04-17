redis.call("SELECT", "1")
rlog.info("Calling processNameOpaque[" .. KEYS[1] .. ", " .. KEYS[2] .. "]")
local metricName = KEYS[1]
local opaqueKey = KEYS[2]
local timestamp = ARGV[1];
local nilv = function(value) 
	if(value == nil) then return 'null' end
	return value
end;

local fireNewMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.NEWMETRIC', cjson.encode(macros.getMetrics(gid)))
end

local fireUpdatedMetric = function(gid)
	redis.call('PUBLISH', 'RINDLE.EVENT.UPDATEDMETRIC', cjson.encode(macros.getMetrics(gid)))
end


if(metricName == 'NULL' and opaqueKey == 'NULL') then 
	return -1
end

local mnId = (metricName ~= 'NULL' and redis.call('get',metricName) or nil)
local okId = (opaqueKey ~= 'NULL' and redis.call('get', opaqueKey) or nil)
local gid = nil
--rlog.info('Keys: MnId:' , nilv(mnId))
--rlog.info('Keys: OkId:', nilv(okId))
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

if(metricName ~= "NULL" and opaqueKey ~= "NULL") then
	-- Both new
	redis.call('hmset', gid, 'n', metricName, 'o', opaqueKey, 'ts', timestamp)
	redis.call('set', opaqueKey, gid)
	redis.call('set', metricName, gid)
elseif(metricName ~= "NULL") then 
	-- metric name was not null
	if(mnId ~= nil) then
		rlog.info('Metric Name Already Assigned');
		-- metric was assigned, opaque was not. Nothing to do
		--rlog.info('Metric Name Already Assigned: [', metricName, ']:', mnId)
		if(opaqueKey ~= 'NULL') then
			redis.call('hmset', mnId, 'o', opaqueKey, 'ts', timestamp)
			redis.call('set', opaqueKey, mnId)
		end;
		return mnId; 		
	else 
		-- metric was not assigned		
		redis.call('hmset', gid, 'n', metricName, 'ts', timestamp)
		redis.call('set', metricName, gid)
		rlog.debug('Metric Name New: [', metricName, ']:', mnId) 		
	end
elseif(opaqueKey ~= "NULL") then 
	-- opaque key was not null
	if(okId ~= nil) then
		rlog.info('Opaque Key Already Assigned');
		-- opaque key was assigned, metric name was not. Nothing to do
		--rlog.info('Opaque Key Already Assigned: [', opaqueKey, ']:', okId)
		if(metricName ~= 'NULL') then
			redis.call('hmset', okId, 'n', metricName, 'ts', timestamp)
			redis.call('set', metricName, okId)
		end;
		return okId; 				 		
	else 
		-- opaque key was not assigned		
		redis.call('hmset', gid, 'o', opaqueKey, 'ts', timestamp)
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



