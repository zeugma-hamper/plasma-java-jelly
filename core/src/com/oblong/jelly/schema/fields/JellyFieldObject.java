package com.oblong.jelly.schema.fields;

import com.oblong.jelly.Slaw;
import com.oblong.jelly.schema.SlawSchema;
import com.oblong.jelly.schema.UnmarshalledSlaw;

/**
 * Created with IntelliJ IDEA.
 * User: karol
 * Date: 9/5/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class JellyFieldObject <T extends UnmarshalledSlaw> extends JellyField<T /* consider SlawMap..? */ > {

	public JellyFieldObject(String name, SlawSchema schema) {
		super(name, schema);
	}

	@Override
	protected T getCustom(Slaw slaw) {
		throw new UnsupportedOperationException("Not implemented yet. I was focused on setting values so far.");
	}

	@Override
	public Slaw toSlaw(T value) {
		return value.toSlaw();
	}
}
