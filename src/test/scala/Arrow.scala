package arrowtest

import org.specs2._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util.Collections
import java.util.Arrays.asList

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.{ FieldVector, IntVector, TinyIntVector, VectorSchemaRoot }
import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }

import org.apache.arrow.vector.ipc.{ ArrowStreamReader, ArrowStreamWriter }

class ArrowSpec extends Specification {

  type BArr = Array[Byte]

  val allocator = new RootAllocator(128)

  def is = s2"""

  ZIO Serdes should
    work with byte arrows             $procRawBytes
    process an empty stream arrow     $procEmptyStream
    process zero length batch         $procStreamZeroLengthBatch

    """

  def procRawBytes = {

    val arrLength = 64

    val expecteds: BArr = Array.fill(arrLength)((scala.util.Random.nextInt(256) - 128).toByte)

    val data = ByteBuffer.wrap(expecteds)

    val buf = allocator.buffer(expecteds.length)
    buf.setBytes(0, data, 0, data.capacity())

    val actuals = new BArr(expecteds.length)
    buf.getBytes(0, actuals)
    expecteds === actuals

  }

  def procEmptyStream = {

    val schema = new Schema(
      asList(new Field("testField", FieldType.nullable(new ArrowType.Int(8, true)), Collections.emptyList()))
    )

    val root: VectorSchemaRoot = VectorSchemaRoot.create(schema, allocator)

    // Write the stream
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    val writer: ArrowStreamWriter  = new ArrowStreamWriter(root, null, out)
    writer.close();

    // check output stream size
    out.size must be_>(0)

    // Read the stream
    val in     = new ByteArrayInputStream(out.toByteArray())
    val reader = new ArrowStreamReader(in, allocator)

    // Check schema
    (schema === reader.getVectorSchemaRoot.getSchema) and
      // Empty should return false
      (reader.loadNextBatch must beFalse) and
      (reader.getVectorSchemaRoot.getRowCount === 0)
  }

  def procStreamZeroLengthBatch = {
    val os = new ByteArrayOutputStream()

    val vector = new IntVector("foo", allocator)
    val schema = new Schema(Collections.singletonList(vector.getField()), null)

    // val root = new VectorSchemaRoot(schema, Collections.singletonList(vector), vector.getValueCount()) // FIXME this doesnt work
    val root: VectorSchemaRoot = VectorSchemaRoot.create(schema, allocator)

    val writer = new ArrowStreamWriter(root, null, Channels.newChannel(os))
    vector.setValueCount(0)
    root.setRowCount(0)
    writer.writeBatch
    writer.end

    val in = new ByteArrayInputStream(os.toByteArray())

    val reader  = new ArrowStreamReader(in, allocator)
    val rroot   = reader.getVectorSchemaRoot
    val rvector = rroot.getFieldVectors.get(0)

    reader.loadNextBatch()

    // should be empty
    (rvector.getValueCount === 0) and
      (rroot.getRowCount === 0)

  }

  def procStreamSocket = {
    import org.apache.arrow.vector.types.Types.MinorType.TINYINT
    import org.apache.arrow.tools.EchoServer

    // BufferAllocator alloc = new RootAllocator(Long.MAX_VALUE);

    val field = new Field(
      "testField",
      new FieldType(true, new ArrowType.Int(8, true), null, null),
      Collections.emptyList()
    )

    val vector = new TinyIntVector("testField", FieldType.nullable(TINYINT.getType()), allocator)
    val schema = new Schema(asList(field))

    // Try an empty stream, just the header.
    // testEchoServer(8080, field, vector, 0)

    true === true
  }

}
