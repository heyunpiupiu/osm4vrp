osm4vrp
====
Fast osm parser for vrp 

Description
======
    osm4vrp is a blazingly fast osm parser for vehicle routing problem

Build and running
========


launch graph extraction program (let /home/graph.osm be the graph location, numb the number of roads, and /home/model.txt the output file)

./sbt "run extract /home/graph.osm numb /home/model.txt"