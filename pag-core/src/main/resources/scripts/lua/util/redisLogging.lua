local rlog = {}

rlog.TRACE = "TRACE"
rlog.DEBUG = "DEBUG"
rlog.INFO = "INFO"
rlog.WARN = "WARN"
rlog.ERROR = "ERROR"
rlog.FATAL = "FATAL"
rlog.OFF = "OFF"

rlog.LOG_LEVELS = {
	TRACE = 1,
    DEBUG = 2,
    INFO = 3,
    WARN = 4,
    ERROR = 5,
    FATAL = 6,
    OFF = 7
}

rlog.LOG_LEVEL_DECODES = {rlog.TRACE, rlog.DEBUG, rlog.INFO, rlog.WARN, rlog.ERROR, rlog.FATAL, rlog.OFF}

rlog.LEVEL = 3

rlog.setLevel = function(level) 
	assert(rlog.LOG_LEVELS[level] ~= nil, "Unknown log level '" .. level .. "'")
	rlog.LEVEL	= rlog.LOG_LEVELS[level]
	return level
end

rlog.tabToString = function(tab, maxDepth, valueDelimiter, lineDelimiter, indent)
    local result = ""
    if (indent == nil) then
        indent = 0
    end
    if (valueDelimiter == nil) then
        valueDelimiter = " = "
    end
    if (lineDelimiter == nil) then
        lineDelimiter = ", "
    end
    if (maxDepth == nil) then
        maxDepth = 2
    end

    for k, v in pairs(tab) do
        if (result ~= "") then
            result = result .. lineDelimiter
        end
        result = result .. string.rep(" ", indent) .. tostring(k) .. valueDelimiter
        if (type(v) == "table") then
            if (maxDepth > 0) then
                result = result .. "{\n" .. rlog.tabToString(v, maxDepth - 1, valueDelimiter, lineDelimiter, indent + 2) .. "\n"
                result = result .. string.rep(" ", indent) .. "}"
            else
                result = result .. "[... more table data ...]"
            end
        elseif (type(v) == "function") then
            result = result .. "[function]"
        else
            result = result .. tostring(v)
        end
    end
    return result
end

rlog.p = function(msg)
    redis.call('PUBLISH', 'RINDLELOG', msg)
end

rlog.ts = function(obj) 
	if(obj == nil) then return 'null' end
	if(type(obj) == 'boolean') then 
		if(obj) then return 'true' 
		else return false
		end
	end
	if(type(obj) ~= 'table') then return tostring(obj) end
	
	return rlog.tabToString(obj)
end


rlog.getLevel = function()
	return rlog.LOG_LEVEL_DECODES[rlog.LEVEL]
end	

rlog._log = function(xlevel, ...)
	if(rlog.LEVEL > xlevel) then return nil end;
	local msg = ''
	local arg = {...}		
	for i,v in ipairs(arg) do
		if(type(v)=='table') then
			for x,y in ipairs(v) do
				msg = msg .. rlog.ts(y)
			end
		else 
			msg = msg .. rlog.ts(v)
		end
	end	
	local event = {}
	event.l = xlevel
	event.m = msg
    redis.call('PUBLISH', 'RINDLE.LOGGING.EVENT.LOG', msg)  -- cjson.encode(event)
	return msg
end

rlog.trace = function(...) 
	rlog._log(1, arg)
end


rlog.debug = function(...) 
	rlog._log(2, arg)
end

rlog.info = function(...)
	rlog._log(3, arg) 
end

rlog.warn = function(...)
	rlog._log(4, arg) 
end

rlog.error = function(...)
	rlog._log(5, arg) 
end

rlog.fatal = function(...) 
	rlog._log(6, arg)
end

rawset(_G, "rlog", rlog)


return "rlog loaded"  --rlog._log('Hello ', 5, ' Jupiter', ' Client:', redis.call('CLIENT', 'GETNAME'), " r:", rlog)
 
