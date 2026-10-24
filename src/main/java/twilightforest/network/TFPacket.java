package twilightforest.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.NetHandler;
import net.minecraft.Packet;

/**
 * The single custom Twilight Forest packet (id allocated by FML, see {@link TFNetwork#register}), server to client only.
 * Payload is {@code data[0]} = discriminator ({@link TFNetwork}) followed by the message body.
 */
public class TFPacket extends Packet {
    private byte[] data = new byte[0];

    /** Required by Packet.getNewPacket (reflective newInstance). */
    public TFPacket() {}

    public TFPacket(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public void readPacketData(DataInput in) throws IOException {
        this.data = new byte[in.readUnsignedShort()];
        in.readFully(this.data);
    }

    @Override
    public void writePacketData(DataOutput out) throws IOException {
        out.writeShort(this.data.length);
        out.write(this.data);
    }

    /** Runs on the main client thread (canProcessAsync is false). */
    @Override
    public void processPacket(NetHandler handler) {
        TFPacketHandler.handle(this.data);
    }

    @Override
    public int getPacketSize() {
        return 2 + this.data.length;
    }
}
