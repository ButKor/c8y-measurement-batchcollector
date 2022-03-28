
# About

This project is supporting to fetch Cumulocity Measurements in batches/chunks. 

It is accepting as input:
* Cumulocity tenant and -authentication information (baseUrl, user, pass)
* A date time span (dateFrom, dateTo)
* A Cumulocity device (optional)
* The maximum limit for measurements per chunk 

...and has as output a list of chunks:
* covering the entire input time span
* being sorted
* with non-colliding time spans (thus no duplicates)
* with each chunk having less than the allowed measurements per chunk

# Logic

The implementation executes below steps:

1) Use the `/measurement/measurements?pageSize=1&withTotalPages=true` endpoint parameters to count the number of measurements for a certain time span **before** actually fetching them
2) Divide the time span recursively as long as the "measurement per time span" are fitting in the allowed chunk-size (input parameter). A binary tree is used as data structure:

![Binary Tree Sample](/resources/imgs/binaryTreeReadme.png)

3) Request and dump all measurements of all chunks sequentially. Result will be a CSV:

```csv
time,source,device_name,fragment.series,value,unit
2022-03-09T21:38:14.568+01:00,100200,test,c8y_Winding.temperature,1.2345,°C
2022-03-11T09:28:18.588+01:00,100200,test,f.s,1.2345,°C
2022-03-11T09:28:28.132+01:00,100200,test,f.s,95,°C
022-03-26T13:06:34.452+01:00,100200,test,f.s,95,°C
2022-03-26T13:06:38.194+01:00,100200,test,f.s,95,°C
etc.
```

# Sample Output

Output using a chunk size o 100,000 on a measurement collection of 611,210 Measurements:
```
*** Measurement Chunk result set ***
Configuration:
------------------------------------------------------------
dateFrom:       2022-03-21T20:00:00.000Z
dateTo:         2022-03-25T20:00:00.000Z
deviceId:       "407401764"
chunkSize:      100,000

Runtime:
------------------------------------------------------------
start:          2022-03-27T13:02:57.729Z
end:            2022-03-27T13:03:00.487Z
duration:       2,758 ms

Results Overview:
------------------------------------------------------------
Count Measurements (total):         611,210
Count Chunks (total):               8
Count Chunks (total, no null):      8
Count exec. data splits (total):    7
Chunk size (max):                   76,719
Chunk size (min, no null):          76,070

Chunks:
dateFrom                              dateTo                                countElements       
-----------------------------------   -----------------------------------   --------------------
2022-03-21T20:00:00.000Z              2022-03-22T08:00:00.000Z                            76,113
2022-03-22T08:00:00.001Z              2022-03-22T20:00:00.000Z                            76,070
2022-03-22T20:00:00.001Z              2022-03-23T08:00:00.000Z                            76,505
2022-03-23T08:00:00.001Z              2022-03-23T20:00:00.000Z                            76,398
2022-03-23T20:00:00.001Z              2022-03-24T08:00:00.000Z                            76,398
2022-03-24T08:00:00.001Z              2022-03-24T20:00:00.000Z                            76,398
2022-03-24T20:00:00.001Z              2022-03-25T08:00:00.000Z                            76,719
2022-03-25T08:00:00.001Z              2022-03-25T20:00:00.000Z                            76,609
```
Note that the chunks seems ~ equally distributed. This is due to the device sending data in a fairly constant frequency.


Output using a chunk size o 1000 on a measurement collection of 11.223 Measurements:

```
*** Measurement Chunk result set ***
Configuration:
------------------------------------------------------------
dateFrom:       2021-01-01T00:00:00.000Z
dateTo:         2022-04-01T00:00:00.000Z
deviceId:       "100200"
chunkSize:      1,000

Runtime:
------------------------------------------------------------
start:          2022-03-27T13:01:56.754Z
end:            2022-03-27T13:02:01.435Z
duration:       4,681 ms

Results Overview:
------------------------------------------------------------
Count Measurements (total):         11,223
Count Chunks (total):               31
Count Chunks (total, no null):      15
Count exec. data splits (total):    30
Chunk size (max):                   947
Chunk size (min, no null):          3

Chunks:
dateFrom                              dateTo                                countElements       
-----------------------------------   -----------------------------------   --------------------
2021-01-01T00:00:00.000Z              2021-08-16T12:00:00.000Z                                 0
2021-08-16T12:00:00.001Z              2021-12-08T06:00:00.000Z                                 0
2021-12-08T06:00:00.001Z              2022-02-03T03:00:00.000Z                                 0
2022-02-03T03:00:00.001Z              2022-03-03T13:30:00.000Z                                 0
2022-03-03T13:30:00.001Z              2022-03-17T18:45:00.000Z                                 3
2022-03-17T18:45:00.001Z              2022-03-24T21:22:30.000Z                                 0
2022-03-24T21:22:30.001Z              2022-03-25T18:42:11.250Z                                 0
2022-03-25T18:42:11.251Z              2022-03-26T05:22:01.875Z                                 0
2022-03-26T05:22:01.876Z              2022-03-26T10:41:57.188Z                                 0
2022-03-26T10:41:57.189Z              2022-03-26T13:21:54.844Z                               502
2022-03-26T13:21:54.845Z              2022-03-26T14:41:53.672Z                                 0
2022-03-26T14:41:53.673Z              2022-03-26T14:51:53.526Z                                 0
2022-03-26T14:51:53.527Z              2022-03-26T14:56:53.453Z                                 0
2022-03-26T14:56:53.454Z              2022-03-26T14:57:30.944Z                                 0
2022-03-26T14:57:30.945Z              2022-03-26T14:57:49.690Z                               192
2022-03-26T14:57:49.691Z              2022-03-26T14:58:08.435Z                               945
2022-03-26T14:58:08.436Z              2022-03-26T14:58:27.181Z                               943
2022-03-26T14:58:27.182Z              2022-03-26T14:58:45.926Z                               946
2022-03-26T14:58:45.927Z              2022-03-26T14:59:04.671Z                               938
2022-03-26T14:59:04.672Z              2022-03-26T14:59:23.416Z                               938
2022-03-26T14:59:23.417Z              2022-03-26T14:59:42.162Z                               947
2022-03-26T14:59:42.163Z              2022-03-26T15:00:00.907Z                               931
2022-03-26T15:00:00.908Z              2022-03-26T15:00:19.653Z                               933
2022-03-26T15:00:19.654Z              2022-03-26T15:00:38.398Z                               944
2022-03-26T15:00:38.399Z              2022-03-26T15:00:57.144Z                               947
2022-03-26T15:00:57.145Z              2022-03-26T15:01:15.889Z                               396
2022-03-26T15:01:15.890Z              2022-03-26T15:01:53.379Z                                 0
2022-03-26T15:01:53.380Z              2022-03-26T15:21:53.086Z                                 0
2022-03-26T15:21:53.087Z              2022-03-26T16:01:52.500Z                                 0
2022-03-26T16:01:52.501Z              2022-03-28T10:41:15.000Z                               718
2022-03-28T10:41:15.001Z              2022-04-01T00:00:00.000Z                                 0
```
Above sample device is sending less constant data -> that's why there are chunks with 0 elements existing. It's configurable to skip chunks of size 0 automatically.