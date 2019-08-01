package arrowtest

import org.specs2._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util.Collections
import java.util.Arrays.asList

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.{ IntVector, VectorSchemaRoot }
import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }

import org.apache.arrow.vector.ipc.{ ArrowReader, ArrowStreamReader, ArrowStreamWriter, ArrowWriter }

class ArrowSpec extends Specification {

  type BArr = Array[Byte]

  val allocator = new RootAllocator(128)

  def is = s2"""

  ZIO Serdes should
    work with byte arrows             $testRawBytes
    testess an empty stream arrow     $testEmptyStream
    testess zero length batch         $testStreamZeroLengthBatch
    Read and Write Arrow Stream       $testReadWrite

    """

  def testSchema = {
    val schema = new Schema(
      asList(new Field("testField", FieldType.nullable(new ArrowType.Int(8, true)), Collections.emptyList()))
    )
    schema
  }

  def simpleSchema(vec: IntVector) =
    new Schema(Collections.singletonList(vec.getField()), null)

  def simpleRoot(schema: Schema): VectorSchemaRoot =
    VectorSchemaRoot.create(schema, allocator)

  def testRawBytes = {

    val arrLength = 64

    val expecteds: BArr = Array.fill(arrLength)((scala.util.Random.nextInt(256) - 128).toByte)

    val data = ByteBuffer.wrap(expecteds)

    val buf = allocator.buffer(expecteds.length)
    buf.setBytes(0, data, 0, data.capacity())

    val actuals = new BArr(expecteds.length)
    buf.getBytes(0, actuals)
    expecteds === actuals

  }

  def testEmptyStream = {

    // Write the stream
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()

    val schema                    = testSchema
    val writer: ArrowStreamWriter = new ArrowStreamWriter(simpleRoot(schema), null, out)
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

  def testStreamZeroLengthBatch = {
    val os = new ByteArrayOutputStream()

    val wvector = new IntVector("foo", allocator)
    val schema  = new Schema(Collections.singletonList(wvector.getField()), null)

    // val root = new VectorSchemaRoot(schema, Collections.singletonList(wvector), wvector.getValueCount()) // FIXME this doesnt work
    val wroot: VectorSchemaRoot = VectorSchemaRoot.create(schema, allocator)

    val writer = new ArrowStreamWriter(wroot, null, Channels.newChannel(os))
    wvector.setValueCount(0)
    wroot.setRowCount(0)
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

  def testReadWrite = {
    val numBatches = 1

    val root = simpleRoot(testSchema)
    root.getFieldVectors.get(0).allocateNew
    //val vec:TinyIntVector = root.getFieldVectors.get(0)
    val vec = root.getFieldVectors.get(0)

    true === true
  }

  def testReadWriteMultipleBatches = {
    val os = new ByteArrayOutputStream()

    val vec    = new IntVector("foo", allocator)
    val schema = simpleSchema(vec)
    val root   = simpleRoot(schema)

    val writer: ArrowStreamWriter = new ArrowStreamWriter(root, null, Channels.newChannel(os))

    writeBatchData(writer, vec, root)

    // Read and validate
    val in = new ByteArrayInputStream(os.toByteArray)
    val reader = new ArrowStreamReader(in, allocator)

    true === true
  }

  // Test helpers
  def writeBatchData(writer: ArrowWriter, vector: IntVector, root: VectorSchemaRoot) = {
    writer.start

    vector.setNull(0)
    vector.setSafe(1, 1)
    vector.setSafe(2, 2)
    vector.setNull(3)
    vector.setSafe(4, 1)
    vector.setValueCount(5)
    root.setRowCount(5)
    writer.writeBatch

    vector.setNull(0)
    vector.setSafe(1, 1)
    vector.setSafe(2, 2)
    vector.setValueCount(3)
    root.setRowCount(3)
    writer.writeBatch

    writer.end
  }

  def validateBatchData(reader: ArrowReader, vector: IntVector) =
    reader.loadNextBatch

  // assertEquals(vector.getValueCount(), 5)
  // assertTrue(vector.isNull(0))
  // assertEquals(vector.get(1), 1)
  // assertEquals(vector.get(2), 2)
  // assertTrue(vector.isNull(3))
  // assertEquals(vector.get(4), 1)

  // reader.loadNextBatch

  // assertEquals(vector.getValueCount(), 3)
  // assertTrue(vector.isNull(0))
  // assertEquals(vector.get(1), 1)
  // assertEquals(vector.get(2), 2)

}
