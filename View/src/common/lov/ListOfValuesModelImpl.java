package common.lov;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ColumnDescriptor;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.ListOfValuesModel;
import oracle.adf.view.rich.model.QueryDescriptor;
import oracle.adf.view.rich.model.QueryModel;
import oracle.adf.view.rich.model.TableModel;

import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;


public class ListOfValuesModelImpl extends ListOfValuesModel {
    //baseModel is for defined for the base component.
    CollectionModel baseModel = new BaseLovCollection();
    //listModel is for the table binding for table in LOV popup dialog   相当于TableModel
    CollectionModel listModel = new ListLovCollection();
    //ListOfValueModel中的对象，下拉框的值
    List _values = new ArrayList();
    List _recentValues = new ArrayList();
    List _recentValues1 = new ArrayList();
    List _filteredList = new ArrayList();
    List<LOVItemSelectionListener> _selectionListeners = new ArrayList<LOVItemSelectionListener>();
    List _listenersFilteredList = new ArrayList();
    //display attributes.
    //String[] _attrs = new String[] { "ename", "job", "sal" };
    String[] _attrs = new String[] { "meaning"};

    List<String> _attributes = new ArrayList<String>();

    public ListOfValuesModelImpl(List<ValueSetRow> valueList) {
        initAttrbiutes();
        initTestData(valueList);
    }

    private void initAttrbiutes() {
        _attributes.add("meaning");
        /*Bug 6909956 - inputcombolistofvalues demo should only show two columns*/
//        _attributes.add("job");
//        _attributes.add("mgr");
//        _attributes.add("hireDate");
//        _attributes.add("sal");
//        _attributes.add("comm");
//        _attributes.add("deptno");
    }

    //初始化下拉框的值
    private void initTestData(List<ValueSetRow> valueList) {
        _values = valueList;
        _filteredList.addAll(_values);
    }

    public void addItemSelectionListener(LOVItemSelectionListener list) {
        _selectionListeners.add(list);
    }
    
    public List suggest(String string) {
        List list  = new ArrayList();
        for(Object obj : _values){
            ValueSetRow vsr = (ValueSetRow)obj;
            if(vsr.getMeaning() == null) continue;
            if(vsr.getMeaning().contains(string)){
                SelectItem sim = new SelectItem(vsr.getMeaning(),vsr.getMeaning()); 
                list.add(sim);
            }    
        }
        return list;
    }

    //模糊查询
    void filterList(String meaning, List filtered) {
        filtered.clear();
        if (meaning != null) {
            for (Object data : _values) {
                if (((ValueSetRow)data).getMeaning().startsWith(meaning)) {
                    filtered.add(data);
                }
            }
        }
        if (filtered.size() == 0) {
            filtered.addAll(_values);
        }
    }

    /**
     * Not applicable as items are only supported in comboLOV
     * @return
     */
    @Override
    public List<? extends Object> getItems() {
        return getValues();
    }

    /**
     * Returns null for now.
     * @return
     */
    @Override
    public QueryModel getQueryModel() {
        return new QueryModelImpl();
    }

    /**
     * @return
     */
    @Override
    public List<? extends Object> getRecentItems() {
        return getRecentValues();
    }

    @Override
    public TableModel getTableModel() {
        return new TableModelImpl(getListModel());
    }

    public CollectionModel getListModel() {
        return listModel;
    }

    @Override
    public List<ColumnDescriptor> getItemDescriptors() {
        List<ColumnDescriptor> descriptors = getTableModel().getColumnDescriptors();
        if (descriptors != null && descriptors.size() > 3) {
            return descriptors.subList(0, 3);
        }
        return descriptors;
    }

    @Override
    public boolean isAutoCompleteEnabled() {
        return false;
    }

    public void performQuery(QueryDescriptor qd) {
        AttributeCriterion criterion = (AttributeCriterion)qd.getConjunctionCriterion().getCriterionList().get(0);
        String ename = (String)criterion.getValues().get(0);
        filterList(ename, _filteredList);
    }

    public List<Object> autoCompleteValue(Object value) {
        // wierd way of filtering and accessing _filteredList but for now its ok
        filterList((String)value, _filteredList);
        if (_filteredList.size() == 1) {
            List<Object> returnList = new ArrayList<Object>();
            ValueSetRow rowData = (ValueSetRow)_filteredList.get(0);
            Object rowKey = rowData.getRowId();
            RowKeySet rowKeySet = new RowKeySetImpl();
            rowKeySet.add(rowKey);
            returnList.add(rowKeySet);
            return returnList;
        }
        return null;
    }

    public void valueSelected(Object value) {
        ValueSetRow rowData = _getRowData(value);
        if (rowData != null) {
            _addToRecentValuesList(rowData, _recentValues);
        }
        Iterator<LOVItemSelectionListener> iter = _selectionListeners.iterator();
        while (iter.hasNext()) {
            LOVItemSelectionListener lit = iter.next();
            lit.valueSelected(rowData);
        }
    }

    private void _addToRecentValuesList(ValueSetRow rowData, List recentValues) {
        if (!recentValues.contains(rowData))
            recentValues.add(0, rowData);

        int size = recentValues.size();
        if (size > 3)
            recentValues.remove(3);
    }

    private ValueSetRow _getRowData(Object selectedRow) {
        if (selectedRow != null && selectedRow instanceof List) {
            List listvalue = (List)selectedRow;
            for (int i = 0; i < listvalue.size(); i++) {
                Object rowData = listvalue.get(i);
                if (rowData instanceof ValueSetRow) {
                    return ((ValueSetRow)rowData);
                }
            }
        } else if (selectedRow != null && selectedRow instanceof RowKeySet) {
            Iterator selection = ((RowKeySet)selectedRow).iterator();
            while (selection.hasNext()) {
                Object rowKey = selection.next();
                Object oldRowKey = listModel.getRowKey();
                listModel.setRowKey(rowKey);
                ValueSetRow rowData = (ValueSetRow)listModel.getRowData();
                listModel.setRowKey(oldRowKey);
                return rowData;
            }
        }
        return null;
    }

    public void validate(FacesContext facesContext, UIComponent uIComponent, Object object) {
        for (Object data : _values) {
            if(((ValueSetRow)data).getMeaning() == null) continue;
            if (((ValueSetRow)data).getMeaning().equals(object)) {
                return;
            }
        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not a Valid Value", "Not a Valid Value");
        throw new ValidatorException(message);
    }

    public List getValues() {
        return _values;
    }

    //最近选择
    public List getRecentValues() {
        List recentValues = new ArrayList();
        recentValues.addAll(_recentValues);

        if (recentValues.size() > 0)
            recentValues.add(new ValueSetRow(null, null, null));

        return recentValues;
    }

    //返回选择的值
    @Override
    public Object getValueFromSelection(Object selectedRow) {
        ValueSetRow rowData = _getRowData(selectedRow);
        if (rowData != null) {
            return rowData.getMeaning();
        }
        return null;
    }


    public QueryDescriptor getQueryDescriptor() {
        if (_queryDescriptor == null)
            _queryDescriptor = new QueryDescriptorImpl();
        return _queryDescriptor;
    }

    private QueryDescriptor _queryDescriptor;
    //private DemoComboboxLOVBean _bean;


    class BaseLovCollection extends CollectionModel {
        public Object getRowKey() {
            if (_row != null) {
                return _row.getRowId();
            }
            return null;
        }

        /**
         * Finds the row with the matching key and makes it current
         * @param rowKey the rowKey, previously obtained from {@link #getRowKey}.
         */
        public void setRowKey(Object rowKey) {
            if (rowKey == null) {
                _row = null;
                return;
            }

            int index = -1;
            for (int i = 0; i < _filteredList.size(); i++) {
                String rowId = ((ValueSetRow)_filteredList.get(i)).getRowId();
                if (rowId.equals(rowKey)) {
                    index = i;
                    break;
                }
            }

            setRowIndex(index);
        }

        public void setRowIndex(int rowIndex) {
            int size = _filteredList.size();
            if (rowIndex < 0 || rowIndex > size || size == 0) {
                _row = null;
                _rowIndex = -1;
            } else {
                _row = (ValueSetRow)_filteredList.get(rowIndex);
                _rowIndex = rowIndex;
            }
        }

        public int getRowIndex() {
            return _rowIndex;
        }

        public Object getRowData() {
            return _row;
        }

        public int getRowCount() {
            return _filteredList.size();
        }

        public boolean isRowAvailable() {
            return (_row != null);
        }

        public Object getRowData(int rowIndex) {
            int oldIndex = getRowIndex();
            try {
                setRowIndex(rowIndex);
                return getRowData();
            } finally {
                setRowIndex(oldIndex);
            }
        }

        public boolean isSortable(String property) {
            return false;
        }

        public List getSortCriteria() {
            return Collections.EMPTY_LIST;
        }

        public Object getWrappedData() {
            return BaseLovCollection.this;
        }

        public void setWrappedData(Object data) {
            throw new UnsupportedOperationException();
        }

        public BaseLovCollection() {
        }

        ValueSetRow _row = null;
        int _rowIndex = -1;
    }

    class ListLovCollection extends CollectionModel {
        public Object getRowKey() {
            if (_row != null) {
                return _row.getRowId();
            }
            return null;
        }

        /**
         * Finds the row with the matching key and makes it current
         * @param rowKey the rowKey, previously obtained from {@link #getRowKey}.
         */
        public void setRowKey(Object rowKey) {
            if (rowKey == null) {
                _row = null;
                return;
            }

            int index = -1;
            for (int i = 0; i < _filteredList.size(); i++) {
                String rowId = ((ValueSetRow)_filteredList.get(i)).getRowId();
                if (rowId.equals(rowKey)) {
                    index = i;
                    break;
                }
            }

            setRowIndex(index);
        }

        public void setRowIndex(int rowIndex) {
            int size = _filteredList.size();
            if (rowIndex < 0 || rowIndex > size || size == 0) {
                _row = null;
                _rowIndex = -1;
            } else {
                _row = (ValueSetRow)_filteredList.get(rowIndex);
                _rowIndex = rowIndex;
            }
        }

        public int getRowIndex() {
            return _rowIndex;
        }

        public Object getRowData() {
            return _row;
        }

        public int getRowCount() {
            return _filteredList.size();
        }

        public boolean isRowAvailable() {
            return (_row != null);
        }

        public Object getRowData(int rowIndex) {
            int oldIndex = getRowIndex();
            try {
                setRowIndex(rowIndex);
                return getRowData();
            } finally {
                setRowIndex(oldIndex);
            }
        }

        public boolean isSortable(String property) {
            return false;
        }

        public List getSortCriteria() {
            return Collections.EMPTY_LIST;
        }

        public Object getWrappedData() {
            return ListLovCollection.this;
        }

        public void setWrappedData(Object data) {
            throw new UnsupportedOperationException();
        }

        ValueSetRow _row = null;
        int _rowIndex = -1;
    }


    /* LOVModel for comboLOV with LaunchPopupListener */


    // For now return a void implementation for the querymodel to show a simple query component
    // such that the Search... link will also be displayed in the dropdown

    public static class QueryModelImpl extends QueryModel {

        public QueryDescriptor create(String name, QueryDescriptor qdBase) {
            return null;
        }

        public void delete(QueryDescriptor qd) {
        }

        public List<AttributeDescriptor> getAttributes() {
            return null;
        }

        public List<QueryDescriptor> getSystemQueries() {
            return null;
        }

        public List<QueryDescriptor> getUserQueries() {
            return null;
        }

        public void reset(QueryDescriptor qd) {
        }

        public void setCurrentDescriptor(QueryDescriptor qd) {
        }

        public void update(QueryDescriptor qd, Map<String, Object> uiHints) {
        }
    }

    // Simple implementation of the QueryDescriptor classs to display one inputText
    // field to filter the data in the table inside dialog based on the Ename

    public static class QueryDescriptorImpl extends QueryDescriptor {
        public QueryDescriptorImpl() {
            _conjCriterion = new ConjunctionCriterionImpl();
        }

        public void addCriterion(String name) {
        }

        public void changeMode(QueryDescriptor.QueryMode mode) {
        }

        public ConjunctionCriterion getConjunctionCriterion() {
            return _conjCriterion;
        }

        public void setConjunctionCriterion(ConjunctionCriterion criterion) {
            _conjCriterion = criterion;
        }

        public String getName() {
            return null;
        }

        public Map<String, Object> getUIHints() {
            return new HashMap<String, Object>();
        }

        public void removeCriterion(oracle.adf.view.rich.model.Criterion object) {
        }

        public AttributeCriterion getCurrentCriterion() {
            return null;
        }

        public void setCurrentCriterion(AttributeCriterion attrCriterion) {
        }

        ConjunctionCriterion _conjCriterion;
    }

    public static class AttributeDescriptorImpl extends AttributeDescriptor {

        public AttributeDescriptor.ComponentType getComponentType() {
            return AttributeDescriptor.ComponentType.inputText;
        }

        public String getDescription() {
            return null;
        }

        public String getFormat() {
            return null;
        }

        public String getLabel() {
            return "meaning";
        }

        public int getLength() {
            return 0;
        }

        public int getMaximumLength() {
            return 0;
        }

        public Object getModel() {
            return null;
        }

        public String getName() {
            return null;
        }

        public Set<AttributeDescriptor.Operator> getSupportedOperators() {
            return null;
        }

        public Class getType() {
            return null;
        }

        public boolean isReadOnly() {
            return false;
        }

        public boolean isRequired() {
            return false;
        }
    }

    public static class ConjunctionCriterionImpl extends ConjunctionCriterion {
        public ConjunctionCriterionImpl() {
            _criterionList = new ArrayList<Criterion>();
            _criterionList.add(new AttributeCriterionImpl());
        }

        public ConjunctionCriterion.Conjunction getConjunction() {
            return ConjunctionCriterion.Conjunction.NONE;
        }

        public List<oracle.adf.view.rich.model.Criterion> getCriterionList() {
            return _criterionList;
        }

        public Object getKey(oracle.adf.view.rich.model.Criterion criterion) {
            return Integer.toString(0);
        }

        public Criterion getCriterion(Object key) {
            assert (_criterionList != null);
            return _criterionList.get(0);
        }

        public void setConjunction(ConjunctionCriterion.Conjunction conjunction) {
        }
        List<Criterion> _criterionList;
    }

    public static class AttributeCriterionImpl extends AttributeCriterion {
        public AttributeCriterionImpl() {
            if (_values == null) {
                _values = new ArrayList<Object>();
                _values.add("A");
            }
        }

        public AttributeDescriptor getAttribute() {
            return new AttributeDescriptorImpl();
        }

        public AttributeDescriptor.Operator getOperator() {
            return null;
        }

        public Map<String, AttributeDescriptor.Operator> getOperators() {
            return null;
        }

        public List<? extends Object> getValues() {
            return _values;
        }

        public boolean isRemovable() {
            return false;
        }

        public void setOperator(AttributeDescriptor.Operator operator) {
        }

        List<Object> _values;
    }

    public class TableModelImpl extends TableModel {
        public TableModelImpl(CollectionModel collectionModel) {
            assert (collectionModel != null);
            _collectionModel = collectionModel;
        }

        @Override
        public CollectionModel getCollectionModel() {
            return _collectionModel;
        }

        @Override
        public List<ColumnDescriptor> getColumnDescriptors() {
            if (_descriptors == null) {
                _descriptors = new ArrayList<ColumnDescriptor>(_attributes.size());
                for (String attr : _attributes) {
                    _descriptors.add(new ColumnDescriptorImpl(attr));
                }
            }
            return _descriptors;
        }

        public class ColumnDescriptorImpl extends ColumnDescriptor {
            public ColumnDescriptorImpl(String name) {
                _name = name;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public String getLabel() {
                return _name.toUpperCase();
            }

            @Override
            public String getName() {
                return _name;
            }

            @Override
            public Class getType() {
                return String.class;
            }

            @Override
            public String getAlign() {
                return null;
            }

            @Override
            public AttributeDescriptor.ComponentType getComponentType() {
                return AttributeDescriptor.ComponentType.inputText;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Set<AttributeDescriptor.Operator> getSupportedOperators() {
                return Collections.emptySet();
            }

            @Override
            public int getLength() {
                return 0;
            }

            public int getMaximumLength() {
                return 0;
            }

            public Object getModel() {
                return null;
            }

            @Override
            public int getWidth() {
                if (_name.equalsIgnoreCase("meaning"))
                    return 12 * 7 + 3;
                else if (_name.equalsIgnoreCase("code"))
                    return 3 * 7 + 3;
//                else if (_name.equalsIgnoreCase("job"))
//                    return 10 * 7 + 3;
//                else if (_name.equals("mgr"))
//                    return 2 * 7 + 3;
                return 0;
            }

            /**
             * The column attributes are all readOnly.
             *
             * @return
             */
            @Override
            public boolean isReadOnly() {
                return true;
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            private String _name;

        }


        private CollectionModel _collectionModel;
        private List<ColumnDescriptor> _descriptors;
    }

}
