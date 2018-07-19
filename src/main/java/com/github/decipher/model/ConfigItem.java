package com.github.decipher.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ConfigItem {

    public static final String STATIC_TYPE = "s";
    public static final String MEMBER_TYPE = "m";

    /**
     * clazz : com.seventh7.widget.iedis.L
     * method : a
     * type : s
     * list : [{"f":1,"s":2},{"f":3,"s":4},{"f":5,"s":6}]
     */

    private String clazz;
    private String method;
    private String type;
    private List<Pair> list;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Pair> getList() {
        return list;
    }

    public void setList(List<Pair> list) {
        this.list = list;
    }

    public boolean checkIsStatic() {
        return STATIC_TYPE.equals(type);
    }

    public static class Pair {
        /**
         * f : 1
         * s : 2
         */

        @JSONField(name = "f")
        private int first;
        @JSONField(name = "s")
        private int second;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public Pair() {
        }

        public int getFirst() {
            return first;
        }

        public void setFirst(int first) {
            this.first = first;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }
    }
}
