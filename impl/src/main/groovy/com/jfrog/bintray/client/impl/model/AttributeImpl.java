package com.jfrog.bintray.client.impl.model;

import com.jfrog.bintray.client.api.model.Attribute;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author jbaruch
 * @since 10/14/14
 */
public class AttributeImpl<T> implements Attribute<T> {


    private Type type;
    private String name;
    private List<T> values;

    public AttributeImpl(String name, T... values) {
        this.name = name;
        this.values = asList(values);
    }

    public AttributeImpl(String name, Type type, T... values) {
        this(name, values);
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<T> values() {
        return values;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributeImpl attribute = (AttributeImpl) o;

        if (!name.equals(attribute.name)) return false;
        if (!values.equals(attribute.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AttributeImpl{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", values=" + values +
                '}';
    }
}
