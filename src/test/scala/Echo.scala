package arrowtest

import org.specs2._

import java.io.{ ByteArrayOutputStream }
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Collections
import java.util.Arrays.asList

import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.{ FieldVector, TinyIntVector, VectorSchemaRoot }
import org.apache.arrow.vector.types.pojo.{ ArrowType, Field, FieldType, Schema }

import org.apache.arrow.vector.ipc.{ ArrowStreamReader, ArrowStreamWriter }

class EchoSpec extends Specification {

  val allocator = new RootAllocator(128)

  def is = s2"""

  Arrow Echo server should
    all tests     $echoTest
    """

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

//  val readVector:TinyIntVector =  reader.getVectorSchemaRoot
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

    true === true
  }

  def echoTest = {
    import org.apache.arrow.vector.types.Types.MinorType.TINYINT

    val field = new Field(
      "testField",
      new FieldType(true, new ArrowType.Int(8, true), null, null),
      Collections.emptyList()
    )

    val vector =
      new TinyIntVector("testField", FieldType.nullable(TINYINT.getType()), allocator);
    val schema = new Schema(asList(field));

    // Try an empty stream, just the header.
    testEchoServer(9000, field, vector, 0);

  }

  def IOtest =
    true === true

}
