package com.oblong.jelly.schema.fields;

import com.oblong.jelly.Slaw;
import com.oblong.jelly.schema.SlawSchema;
import com.oblong.jelly.slaw.java.SlawMap;

/**
 * Created with IntelliJ IDEA.
 * User: karol
 * Date: 7/10/13
 * Time: 1:38 PM
 */
public class MapField extends AbstractField<SlawMap> {

	public MapField(String name, SlawSchema schema) {
		super(name, schema);
	}

	public MapField(String name) {
		super(name);
	}

	@Override
	protected SlawMap getCustom(Slaw slaw) {
		return (SlawMap) slaw;
	}

	@Override
	public Slaw toSlaw(SlawMap value) {
		return value;
	}
}