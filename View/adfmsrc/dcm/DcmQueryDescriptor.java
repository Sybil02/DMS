package dcm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;
import oracle.adf.view.rich.model.QueryDescriptor;

public class DcmQueryDescriptor extends FilterableQueryDescriptor {
    private Map<String, Object> filterCriteria;
    private ConjunctionCriterion conjunctionCriterion=new CustomConjunctionCriterion();
    public DcmQueryDescriptor() {
    }

    public Map<String, Object> getFilterCriteria() {
        return this.filterCriteria;
    }

    public void setFilterCriteria(Map<String, Object> map) {
        this.filterCriteria=map;
    }

    public void addCriterion(String string) {
        
    }

    public void changeMode(QueryDescriptor.QueryMode queryMode) {
    }

    public ConjunctionCriterion getConjunctionCriterion() {
        return this.conjunctionCriterion;
    }

    public String getName() {
        return null;
    }

    public Map<String, Object> getUIHints() {
        return Collections.emptyMap();
    }

    public void removeCriterion(oracle.adf.view.rich.model.Criterion criterion) {
    }

    public AttributeCriterion getCurrentCriterion() {
        return null;
    }

    public void setCurrentCriterion(AttributeCriterion attributeCriterion) {
    }
    
    
    private class CustomConjunctionCriterion extends ConjunctionCriterion{
        private ConjunctionCriterion.Conjunction conjunction=ConjunctionCriterion.Conjunction.AND;
        private List<oracle.adf.view.rich.model.Criterion> criterionList;
        public ConjunctionCriterion.Conjunction getConjunction() {
            return this.conjunction;
        }
    
        public List<oracle.adf.view.rich.model.Criterion> getCriterionList() {
            return this.criterionList;
        }
    
        public Object getKey(oracle.adf.view.rich.model.Criterion criterion) {
            if(this.criterionList!=null){
                for(int i=0;i<this.criterionList.size();i++){
                    if(this.criterionList.get(i).equals(criterion)){
                        return i;
                    }
                }
            }
            return null;
        }
    
        public oracle.adf.view.rich.model.Criterion getCriterion(Object object) {
            if(this.criterionList==null||this.criterionList.size()<=(Integer)object){
                return null;
            }
            return this.criterionList.get((Integer)object);
        }
    
        public void setConjunction(ConjunctionCriterion.Conjunction conjunction) {
            this.conjunction=conjunction;
        }
    }
}

