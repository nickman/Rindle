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
        lineDelimiter = "\n"
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
    redis.call('PUBLISH', 'RINDLELOG', msg);
end;

rlog.ts = function(obj) 
	if(obj == nil) then return 'null' end
	if(type(obj) ~= 'table') then return tostring(obj) end
	return rlog.tabToString(obj);
end;


rlog.getLevel = function()
	return rlog.LOG_LEVEL_DECODES[rlog.LEVEL];
end	

rlog._log = function(...) 
	local msg = ''		
	for i,v in ipairs(arg) do
		msg = msg .. rlog.ts(v)
	end
	--redis.call('PUBLISH', 'RINDLELOG:' .. tostring(redis.call('CLIENT', 'GETNAME')), msg);
    redis.call('PUBLISH', 'RINDLELOG', msg);
	return msg;
end;

rlog.trace = function(...) 
	if(rlog.LEVEL <= 1) then
		rlog._log(arg)
	end;
end


rlog.debug = function(...) 
	if(rlog.LEVEL <= 2) then
		rlog._log(arg)
	end;
end

rlog.info = function(...) 
	if(rlog.LEVEL <= 3) then
		rlog._log(arg)
	end;
end

rlog.warn = function(...) 
	if(rlog.LEVEL <= 4) then
		rlog._log(arg)
	end;
end

rlog.error = function(...) 
	if(rlog.LEVEL <= 5) then
		rlog._log(arg)
	end;
end

rlog.fatal = function(...) 
	if(rlog.LEVEL <= 6) then
		rlog._log(arg)
	end;
end

--local function alex() return 3.1415 end
rawset(_G, "rlog", rlog)


return "rlog loaded"  --rlog._log('Hello ', 5, ' Jupiter', ' Client:', redis.call('CLIENT', 'GETNAME'), " r:", rlog);
 
