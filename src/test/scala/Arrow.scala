package arrowtest

import org.specs2._

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Collections
import java.util.Arrays.asList

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.{ FieldVector, TinyIntVector, VectorSchemaRoot }
import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }

import org.apache.arrow.vector.ipc.{ ArrowStreamReader, ArrowStreamWriter }

class ArrowSpec extends Specification {

  type BArr = Array[Byte]

  val allocator = new RootAllocator(128)

  def is = s2"""

  ZIO Serdes should
    work with byte arrows             $procRawBytes
    process an empty stream arrow     $procEmptyStream

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

  def procStreamZeroLengthBatch =
    // val os = new ByteArrayOutputStream()

    // val vector = new IntVector("foo", allocator)
    // val schema = new Schema(Collections.singletonList(vector.getField()), null)

    // val root = new VectorSchemaRoot(schema, Collections.singletonList(vector), vector.getValueCount())

    // val writer = new ArrowStreamWriter(root, null, Channels.newChannel(os))) {
    // val writer = new ArrowStreamWriter(root, null, null)
    // {
    //   vector.setValueCount(0)
    //   root.setRowCount(0)
    //   writer.writeBatch()
    //   writer.end()
    // }

    // ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());

    // try (ArrowStreamReader reader = new ArrowStreamReader(in, allocator);) {
    //   VectorSchemaRoot root = reader.getVectorSchemaRoot();
    //   IntVector vector = (IntVector) root.getFieldVectors().get(0);
    //   reader.loadNextBatch();
    //   assertEquals(vector.getValueCount(), 0);
    //   assertEquals(root.getRowCount(), 0);
    true === true

  def testEchoServer(serverPort: Int, field: Field, vector: FieldVector, batches: Int) = {
    val size = 16

    val root = new VectorSchemaRoot(asList(field), asList(vector), 0)
// try (BufferAllocator alloc = new RootAllocator(Long.MAX_VALUE);
    val socket = new Socket("localhost", serverPort);
    val writer = new ArrowStreamWriter(root, null, socket.getOutputStream())
    val reader = new ArrowStreamReader(socket.getInputStream(), allocator)
    writer.start()
    // vector.allocateNew(16)

    // for (int i = 0; i < batches; i++) {
// vector.allocateNew(16);
// for (int j = 0; j < 8; j++) {
// vector.set(j, j + i);
// vector.set(j + 8, 0, (byte) (j + i));
// }

    vector.setValueCount(size)
    root.setRowCount(size)
    writer.writeBatch()
    writer.end()

    new Schema(asList(field)) === reader.getVectorSchemaRoot().getSchema()

// TinyIntVector readVector = (TinyIntVector) reader.getVectorSchemaRoot()
// .getFieldVectors().get(0);
// for (int i = 0; i < batches; i++) {
// Assert.assertTrue(reader.loadNextBatch());
// assertEquals(16, reader.getVectorSchemaRoot().getRowCount());
// assertEquals(16, readVector.getValueCount());
// for (int j = 0; j < 8; j++) {
// assertEquals(j + i, readVector.get(j));
// assertTrue(readVector.isNull(j + 8));
// }
// }
// Assert.assertFalse(reader.loadNextBatch());
// assertEquals(0, reader.getVectorSchemaRoot().getRowCount());
// assertEquals(reader.bytesRead(), writer.bytesWritten());

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
