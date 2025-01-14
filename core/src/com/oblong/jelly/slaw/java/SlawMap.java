// Copyright (c) 2010 Oblong Industries

package com.oblong.jelly.slaw.java;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oblong.jelly.ISlawMap;
import com.oblong.jelly.NumericIlk;
import com.oblong.jelly.Slaw;
import com.oblong.jelly.SlawIlk;

//import com.oblong.jelly.schema.fields.AbstractField;
import net.jcip.annotations.Immutable;

@Immutable
public final class SlawMap extends JavaSlaw implements ISlawMap {

    static SlawMap valueOf(Map<Slaw,Slaw> map) {
        if (map == null) return EMPTY_MAP;
        List<Slaw> ls = new ArrayList<Slaw>(map.size());
        for (Map.Entry<Slaw,Slaw> e : map.entrySet()) {
            Slaw key = e.getKey();
            Slaw value = e.getValue();
            if (key != null && value != null)
                ls.add(SlawCons.valueOf(key, value));
        }
        return new SlawMap(ls);
    }

    static Slaw valueOf(List<Slaw> e) {
        return new SlawMap(
            SlawList.listOfConses(e) ? filterConses(e) : listToConses(e));
    }

    @Override public SlawIlk ilk() { return SlawIlk.MAP; }
    @Override public NumericIlk numericIlk() { return null; }

    @Override public int dimension() { return 0; }
    @Override public int count() { return conses.count(); }
    @Override public Slaw nth(int n) { return conses.nth(n); }
    @Override public Slaw find(Slaw k) {
        if (k != null) {
            for (int i = 0, c = count(); i < c; i++)
                if (k.equals(nth(i).car())) return nth(i).cdr();
        }
        return null;
    }

    @Override public Slaw car() { return conses.car(); }
    @Override public Slaw cdr() { return conses.cdr(); }
    @Override public Map<Slaw,Slaw> emitMap() { return conses.emitMap(); }

    @Override public boolean slawEquals(Slaw o) {
        if (o.count() != conses.count()) return false;
        for (int i = 0, c = count(); i < c; i++) {
            final Slaw nth = conses.nth(i);
            final Slaw key = nth.car();
            final Slaw oval = o.find(key);
            if (oval == null || !oval.equals(nth.cdr())) return false;
        }
        return true;
    }

    @Override public int hashCode() { return 3 + 11 * conses.hashCode(); }

    private static List<Slaw> filterConses(List<Slaw> e) {
        List<Slaw> conses = new ArrayList<Slaw>(e.size());
        Set<Slaw> keys = new HashSet<Slaw>(e.size());
        for (int i = e.size() - 1; i >= 0; i--) {
            Slaw s = e.get(i);
            Slaw k = s.car();
            if (k != null && s.cdr() != null && keys.add(k)) conses.add(s);
        }
        return conses;
    }

    private static List<Slaw> listToConses(List<Slaw> e) {
        List<Slaw> conses = new ArrayList<Slaw>(e.size() / 2);
        Set<Slaw> keys = new HashSet<Slaw>(e.size() / 2);
        for (int i = e.size() / 2; i > 0; i--) {
            Slaw k = e.get(2 * i - 2);
            Slaw v = e.get(2 * i - 1);
            if (k != null && v != null && keys.add(k))
                conses.add(SlawCons.valueOf(k, v));
        }
        return conses;
    }

    private static final SlawMap EMPTY_MAP =
        new SlawMap(new ArrayList<Slaw>());

    private SlawMap(List<Slaw> elems) {
        conses = new SlawList(elems, false);
    }

    private final SlawList conses;

//	@Override
//	public <Type> Type get(AbstractField<Type> field) {
//		return field.getFrom(this);
//	}

	@Override
	public Map<Slaw, Slaw> emitContainedMap() {
		return emitMap();
	}
}
