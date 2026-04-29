/*
 * Decompiled with CFR 0.152.
 */
package r0se.api.render;

public enum Easing {
    LINEAR{

        @Override
        public double ease(double factor) {
            return factor;
        }
    }
    ,
    SINE_IN_OUT{

        @Override
        public double ease(double factor) {
            return -(Math.cos(Math.PI * factor) - 1.0) / 2.0;
        }
    }
    ,
    CUBIC_OUT{

        @Override
        public double ease(double factor) {
            return 1.0 - Math.pow(1.0 - factor, 3.0);
        }
    }
    ,
    SMOOTH_STEP{

        @Override
        public double ease(double factor) {
            return factor * factor * (3.0 - 2.0 * factor);
        }
    };


    public abstract double ease(double var1);
}

