package laas.fspex.parser

/**
 * Created by Ulrich Matchi Aïvodji on 16/04/2015.
 */


import laas.fspex.model._
import laas.fspex.utils._

import scala.collection.mutable
import scala.io.Source
import scala.xml.MetaData
import scala.xml.pull._

import scala.collection.mutable

import scala.io.Source
import scala.xml.MetaData
import scala.xml.pull._

import java.io._

import scala.util.Random

object OsmParser {

  def getAttrib(attrs:MetaData, name:String) = {
    val attr = attrs(name)
    if(attr == null) {
      sys.error(s"Expected attribute $name does not exist")
    }
    if(attr.length > 1) {
      sys.error(s"Expected attribute $name has more than one return.")
    }
    attr(0).text
  }

  def parseNode(attrs:MetaData,nodes:mutable.Map[String,Vertex]) = {
    val id = getAttrib(attrs, "id")
    val lat = getAttrib(attrs,"lat").toDouble
    val lon = getAttrib(attrs,"lon").toDouble

    nodes(id) = StreetVertex(Location(lat,lon),id)
  }


  def parseRoad(parser:XMLEventReader,
               wayAttribs:MetaData,
               nodes:mutable.Map[String,Vertex],
                roads:mutable.Map[String,Road]
               )= {
    val wayNodes = mutable.ListBuffer[Vertex]()
    var break = !parser.hasNext
    var wayInfo:WayInfo = null

    val wayId = getAttrib(wayAttribs,"id")

    val tags = mutable.Map[String,String]()

    while(!break) {
      parser.next match {
        case EvElemStart(_,"nd",attrs,_) =>
          val id = getAttrib(attrs,"ref")
          if(nodes.contains(id)) {
            val v=nodes(id)
            wayNodes += v
          }
        case EvElemStart(_,"tag",attrs,_) =>
          val k = getAttrib(attrs,"k")
          val v = getAttrib(attrs,"v")
          tags(k) = v
        case EvElemEnd(_,"way") => wayInfo = WayInfo.fromTags(wayId,tags.toMap)

          if(wayInfo.isDrivable) {
            var name="Rue sans nom"
            if (wayInfo.tags.contains("name")){
              name=wayInfo.tags("name")
            }
            var distance=0.0
            wayNodes.reduceLeft { (v1,v2) =>
              distance+=Distance.distance(v1.location,v2.location)
              v2}

            roads(wayId)=Road(name,wayInfo.tags("highway"),wayNodes(0),wayNodes(wayNodes.length-1),distance)
          }
          break = true
        case _ => // pass
      }
      break = break || !parser.hasNext
    }

  }



  def vrp(osmPath:String, numb:Int, save:String)= {
    val nodes = mutable.Map[String,Vertex]()
    val roads= mutable.Map[String,Road]()

    Logger.timed("Parsing OSM XML into nodes and roads...",
      "OSM XML parsing complete.") { () =>
      val source = Source.fromFile(osmPath)

      try {
        val parser = new XMLEventReader(source)
        while(parser.hasNext) {
          parser.next match {

            case EvElemStart(_,"node",attrs,_) =>
              parseNode(attrs,nodes)
            case EvElemStart(_,"way",attrs,_) => parseRoad(parser,attrs,nodes,roads)

            case _ => //pass
          }
        }
      } finally {
        source.close
      }
    }

    /*for (r<-roads){
      println(r)
    }*/

    val myRoads = mutable.ListBuffer[vrpRoad]()

    for (r<-roads){
      myRoads+= vrpRoad(r._1, r._2.name, r._2.highway, r._2.begin, r._2.end, r._2.distance)
    }

    val gen =Random

    val writer = new PrintWriter(new File(save))


    writer.write("id || name ||start|| end || type || distance(m)\n")

    for(i<-1 to numb){
      val road=myRoads(gen.nextInt(myRoads.length))
      writer.write(road.id +"||" + road.name +"||" + road.begin +"||"+ road.end +"||"+ road.highway +"||"+ road.distance +"\n")
    }

    writer.close()




  }
}
