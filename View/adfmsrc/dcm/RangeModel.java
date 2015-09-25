package dcm;

import org.apache.myfaces.trinidad.model.BoundedRangeModel;

    public class RangeModel extends BoundedRangeModel {
        
        long maxinum = -1;
        long value = -1;
        
        public long getMaximum() {
            return maxinum;
        }

        public long getValue() {
            return value;
        }
        
        public void setMaximum(long maxinum) {
            this.maxinum = maxinum;
        }
        
        public void setValue(long value) {
            this.value = value;
        }
        
    }