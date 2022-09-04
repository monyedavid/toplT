package org.topl.traffic.protocol.models

trait JsonSource

case class FileJsonSource(filePath: String) extends JsonSource

case class ExtJsonSource(uri: String) extends JsonSource
