redis.call("SELECT", rindle.db)

local metricName = rindle.nprefix(KEYS[1])
local opaqueKey = rindle.oprefix(KEYS[2])
local dn = metricName ~= nil and metricName or 'nil'
local dok = opaqueKey ~= nil and opaqueKey or 'nil'
rlog.debug("---> process mn:" .. dn .. " ok:" .. dok)
local timestamp = ARGV[1]
local mnId = (metricName ~= nil and redis.call('get',metricName) or nil)
local okId = (opaqueKey ~= nil and redis.call('get', opaqueKey) or nil)

return rindle.gunfix(rindle.process(metricName, mnId, opaqueKey, okId, timestamp))





