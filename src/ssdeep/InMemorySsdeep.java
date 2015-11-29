package ssdeep;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InMemorySsdeep extends ssdeep{
	
	protected int ss_update(ss_context ctx, InputStream stream) throws IOException{
        int bytes_read;
        byte[] buffer;

        if (null == ctx || null == stream)
        {
                return 1;
        }

        buffer = new byte[BUFFER_SIZE];

        ctx.p = new byte[SPAMSUM_LENGTH + 1];
        ctx.ret2 = new byte[SPAMSUM_LENGTH / 2 + 1];

        ctx.k = ctx.j = 0;
        ctx.h3 = ctx.h2 = HASH_INIT;
        ctx.h = roll_reset();

        while ((bytes_read = stream.read(buffer, 0, buffer.length)) > 0)
        {
            ss_engine(ctx, buffer, bytes_read);
        }

        if (ctx.h != 0)
        {
            ctx.p[ctx.j] = b64[(int)(ctx.h2 % 64)];
            ctx.ret2[ctx.k] = b64[(int)(ctx.h3 % 64)];
            ctx.j++;
            ctx.k++;
        }
        
        ctx.signature = new SpamSumSignature(ctx.block_size, GetArray(ctx.p, (int)ctx.j), GetArray(ctx.ret2, (int)ctx.k));

        return 0;
    }
	
	public String fuzzy_hash_array(byte[] src) throws IOException{
        if (src == null){
            throw new IllegalArgumentException("stream");
        }

        ss_context ctx = new ss_context();
        ss_init(ctx, src.length);
    	ByteArrayInputStream stream = new ByteArrayInputStream(src);
    	
        do{
        	stream.reset();
            ss_update(ctx, stream);
            ctx.block_size = ctx.block_size / 2;
        }while(ctx.block_size > MIN_BLOCKSIZE && ctx.j < SPAMSUM_LENGTH / 2);

        return ctx.signature.toString();
    }
	
}
