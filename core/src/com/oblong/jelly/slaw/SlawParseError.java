// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw;

import com.oblong.jelly.Slaw;

import java.nio.ByteBuffer;

/**
 *
 * Created: Tue May 25 17:05:57 2010
 *
 * @author jao
 */
public final class SlawParseError extends Exception {

    public SlawParseError(int pos, String msg) {
        super("Error: " + msg + " at position " + pos);
        position = pos;
    }

    public SlawParseError(ByteBuffer b, String msg) {
        super("Error: " + msg + " at position " + b.position());
        position = b.position();
    }

    public SlawParseError(int pos) {
        super("Error at position " + pos);
        position = pos;
    }

    public int position() { return position; }

    private final int position;
    private static final long serialVersionUID = 5363556892136529454L;


}
