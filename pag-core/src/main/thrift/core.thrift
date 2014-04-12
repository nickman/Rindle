namespace * org.helios.rindle.core.datapoints.thrift


	union DataPointValue {
		/** A 64 bit long value */
		1: 	i64 longValue			// A 64 bit long value
		/** A double value */
		2:	double doubleValue		// A double value
	}
	
	struct DataPoint {
		1:	i64 golbalID			// The global unique identifier for the datapoint type
		2:	i64	timestamp			// The timestamp of the datapoint as a UTC i64
		3:	DataPointValue value  	// The DataPointValue for this data point
	}
 
	service StreamNormalizer {
		i64 assignClientId(
			1: string address
			2: i32 port
		);
	  /**
		* Submits a single data point
		*
		* @param client The client identifier
		* @param dataPoint The data point to submit
		*/	
		oneway void submitDataPoint(
			1: i64 client
			2: DataPoint dataPoint);
		oneway void submitDataPoints(
			1: i64 client
			2: set<DataPoint> dataPoints);
		void subscribe(
			1: i64 client
			2: set<i64> points
			3: i32 options
			4: i64 period);
		oneway void cancel(
			1: i64 client
			2: set<i64> points);
		oneway void cancelAll(
			1: i64 client);
		oneway void bye(
			1: i64 client);



	}

