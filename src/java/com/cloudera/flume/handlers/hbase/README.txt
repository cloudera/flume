Running Flume on HBase
----------------------

Here's how I've run flume against hbase in dev mode:

1) download hbase 0.20.4 and start it via "bin/start-hbase.sh"

Note that starting hbase will also start ZooKeeper

2) using the hbase shell create a table for the flume sink to write events

$ bin/hbase shell
> create 't1', 'f1'

3) start flume, I started a node with a console source and hbase sink

$ FLUME_DEVMODE=true bin/flume master_nowatch
$ FLUME_DEVMODE=true bin/flume shell
> connect localhost
> exec config hbase_sink_node 'console' 'hbase("t1","f1")'
> quit

$ FLUME_DEVMODE=true bin/flume node_nowatch -n hbase_sink_node

4) enter some events on hbase_sink_node (type some text followed by return)

5) again using the hbase shell scan the output table for your rows

> scan 't1'

example output: (I typed "hello" on hbase_sink_node console input)

hbase(main):002:0> scan 't1'
ROW                          COLUMN+CELL                                                                      
 \x00\x00\xF6_\x0Fk\xF4\x80  column=f1:event, timestamp=1274227444388, value=hello                            
 \x00\x00\xF6_\x0Fk\xF4\x80  column=f1:host, timestamp=1274227444388, value=valhalla                          
 \x00\x00\xF6_\x0Fk\xF4\x80  column=f1:timestamp, timestamp=1274227444388, value=\x00\x00\x01\x28\xAD\xDF\xCA\
                             x7C                                                                              
1 row(s) in 0.0550 seconds
