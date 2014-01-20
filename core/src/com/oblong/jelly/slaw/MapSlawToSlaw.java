package com.oblong.jelly.slaw;

import com.oblong.jelly.Slaw;
import com.oblong.jelly.schema.fields.AbstractField;
import com.oblong.jelly.slaw.java.SlawMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: karol
 * Date: 9/2/13
 * Time: 1:09 PM
 *
 */
public class MapSlawToSlaw {
	protected Map<Slaw, Slaw> map = new HashMap<Slaw,Slaw>();

	public <T> void put(AbstractField<T> field, T value) {
		map.put(field.getNameSlaw(), field.toSlaw(value));

		// consider what happens if value == null ?
	}

	public SlawMap toSlaw() {
		return Slaw.map(this.map);
	}
}
