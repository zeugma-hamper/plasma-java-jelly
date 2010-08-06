// Copyright (c) 2010 Oblong Industries
// Created: Wed Jun  9 16:20:42 2010

package com.oblong.jelly;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

/**
 * A wrapper around URIs representing pool server addresses.
 *
 * <p> To connect to a pool server we need to know its host, a port
 * and the protocol or scheme it uses. All these three parameters can
 * be specified in one shot as a string, but sometimes it's handy to
 * use a more structured data. PoolServerAddress offers the latter.
 *
 * @author jao
 */
@Immutable
public final class PoolServerAddress {

    /** Default connection port for server addresses ({@value}). */
    public static final int DEFAULT_PORT = 65456;

    /** Default scheme for server addresses ({@value}). */
    public static final String DEFAULT_SCHEME = "tcp";

    /** Default host for server addresses ({@value}). */
    public static final String DEFAULT_HOST = "localhost";

    /**
     * Creates a pool address by parsing the given string.
     *
     * <p> If either the hostname or the port are missing, default
     * values ({@link #DEFAULT_HOST} and {@link #DEFAULT_PORT},
     * respectively) are used. Thus, "tcp://", "tcp://localhost" and
     * "tcp://localhost:65456" represent the same pool address.
     *
     * <p> The expected format and parsing rules are, for the most
     * part, those used for the server part of URLs. Note that the
     * URI's scheme part is mandatory.
     *
     * @throws BadAddressException if {@code uri} is not a valid
     * server URI.
     */
    public static PoolServerAddress fromURI(String uri)
        throws BadAddressException {
        Matcher matcher = ADDR_PATT.matcher(uri);
        if (!matcher.lookingAt())
            throw new BadAddressException("Malformed server URI: " + uri);
        final String scheme = matcher.group(1);
        final String host = matcher.group(2);
        final String port = matcher.group(3);
        final int p = port == null ? -1 : Integer.parseInt(port);
        return new PoolServerAddress(scheme, host, p);
    }

    /**
     * Constructs a new server address out of its three components.
     * Any of its first two arguments can be null, in which case the
     * default values given by {@code DEFAULT_SCHEME} and {@code
     * DEFAULT_HOST} are used. If {@code port} is negative, {@code
     * DEFAULT_PORT} is used instead.
     *
     * <p> {@code host} can be either a human readable hostname or a
     * string representation of an IP address (for schemes, such as
     * TCP, using that addressing protocol).
     *
     * @throws BadAddressException {@code uri} is {@code host} is not
     * a valid host name.
     */
    public PoolServerAddress(String scheme, String host, int port)
        throws BadAddressException {
         this.scheme = checkScheme(scheme);
         this.host = checkHost(host);
         this.port = port < 0 ? DEFAULT_PORT : port;
         stringRep = this.scheme
             + (this.host.isEmpty() ? "" : ("://" + this.host))
             + (this.port == DEFAULT_PORT ? "" : ":" + this.port);
    }

    /**
     * Equivalent to {@code PoolServerAddress(DEFAULT_SCHEME, host,
     * port)}.
     *
     * @see #PoolServerAddress(String, String, int)
     */
    public PoolServerAddress(String host, int port)
        throws BadAddressException {
        this("", host, port);
    }

    /**
     * Equivalent to {@code PoolServerAddress(DEFAULT_SCHEME, host,
     * DEFAULT_PORT)}.
     *
     * @see #PoolServerAddress(String, String, int)
     */
    public PoolServerAddress(String host) throws BadAddressException {
        this("", host, -1);
    }

    public String scheme() { return scheme; }

    public String host() { return host; }

    public int port() { return port; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof PoolServerAddress)) return false;
        final PoolServerAddress oa = (PoolServerAddress)o;
        return stringRep.equals(oa.stringRep);
    }

    @Override public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns a valid URI representing this address.
     * <p> That means that it is always the case that:
     * <pre>
     *   address == PoolServerAddress.fromURI(address.toString());
     * </pre>
     */
    @Override public String toString() {
        return stringRep;
    }

    /**
     * Auxliarily function to help determine whether a URI is contains
     * a pool address part or should be considered just a pool name.
     * Client code probably won't use this method at all.
     */
    public static boolean isRelative(String uri) {
        return !ADDR_PATT.matcher(uri).lookingAt();
    }

    static final String ADDR_REGEX =
        "(?:([\\p{Alpha}]+)://)(?:([^ :/]+)(?::(\\d+))?)?";

    private static final Pattern ADDR_PATT = Pattern.compile(ADDR_REGEX);

    private static String checkScheme(String scm) throws BadAddressException {
        if (scm == null || scm.isEmpty()) return DEFAULT_SCHEME;
        if (scm.indexOf(':') > 0 || scm.indexOf('/') > 0)
            throw new BadAddressException("Scheme cannot contain ' or /");
        return scm;
    }

    private static String checkHost(String host) throws BadAddressException {
        if (host == null || host.isEmpty()) return DEFAULT_HOST;
        if (host.indexOf(':') > 0 || host.indexOf('/') > 0)
            throw new BadAddressException("Host cannot contain `:' or `/'");
        return host;
    }

    private final String scheme;
    private final String host;
    private final int port;
    private final String stringRep;
}
