package com.oblong.jelly.schema.fields;

import com.oblong.jelly.Slaw;
import com.oblong.jelly.schema.SlawSchema;
import com.oblong.jelly.slaw.java.SlawList;
import com.oblong.util.logging.ObLog;

/**
 * Created with IntelliJ IDEA.
 * User: karol
 * Date: 7/12/13
 * Time: 2:29 PM
 *
 */
public class RawSlawListField extends AbstractField<SlawList> {

    private final ObLog log = ObLog.get(this);

    public RawSlawListField(String name) {
        super(name);
    }

    public RawSlawListField(SlawSchema schema, boolean isOptional, String name) {
        super(schema, isOptional, name);
    }

    @Override
    protected SlawList getCustom(Slaw slaw) {
        return (SlawList) slaw;
    }

    @Override
    public Slaw toSlaw(SlawList value) {
        return value;
    }

}