 /** Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved. */
 package dcm;

import common.JSFUtils;

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
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

 import oracle.adf.view.rich.component.rich.data.RichTable;
 import oracle.adf.view.rich.event.ReturnPopupEvent;
 import oracle.adf.view.rich.component.rich.input.RichInputComboboxListOfValues;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.LaunchPopupEvent;
 import oracle.adf.view.rich.event.LaunchPopupListener;
 import oracle.adf.view.rich.model.AttributeCriterion;
 import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.AutoSuggestUIHints;
import oracle.adf.view.rich.model.ColumnDescriptor;
 import oracle.adf.view.rich.model.ConjunctionCriterion;
 import oracle.adf.view.rich.model.Criterion;
 import oracle.adf.view.rich.model.ListOfValuesModel;
 import oracle.adf.view.rich.model.QueryDescriptor;
 import oracle.adf.view.rich.model.QueryModel;
 import oracle.adf.view.rich.model.TableModel;

 import org.apache.myfaces.trinidad.event.SelectionEvent;
 import org.apache.myfaces.trinidad.model.CollectionModel;
 import org.apache.myfaces.trinidad.model.RowKeySet;
 import org.apache.myfaces.trinidad.model.RowKeySetImpl;

 public class ComboboxLOVBean implements Validator
 {
     
     public ComboboxLOVBean(List<SelectItem> list, List<Attribute> attrName )
     {
         this(list, attrName, 0);
         
     }
     //第三个参数是第几个列参数是作为搜索，选择的列
   public ComboboxLOVBean(List<SelectItem> list, List<Attribute> attrName, int valCol)
   { 
       setAttributes(attrName);
       if(attrName != null )
          setLabel(attrName.get(valCol).getLabel()); 
       
        if(list != null)
            for (SelectItem item : list)
            {
              FileData data = new FileData(item.getValue().toString(), item.getLabel().toString());
              _values.add(data);  
            }  
        _filteredList.addAll(_values);
    } 

   //------------------public API for binding --------------------------------//    
   private String _name; 
   //验证程序提示显示的列名
   private String _label;
   
   public void setName(Object name)
   {  
      _name = name.toString();
   }

   public String getName()
   { 
      return _name;
   }
    
     public void setLabel(String label)
     {
       _label = label;
     }

     public String getLabel()
     {
       return _label;
     }
     
   private Object returnPopupDataValue;
     
   public void setReturnPopupDataValue(Object returnPopupDataValue) 
   {
       this.returnPopupDataValue = returnPopupDataValue;
   }

   public Object getReturnPopupDataValue() 
   {
       return returnPopupDataValue;
   }
   
   private Object returnPopupDataValue1;
   
   public void selected(SelectionEvent event)
   {
     setReturnPopupDataValue(event.getAddedSet());
   }
   
   public void returnPopupListener(ReturnPopupEvent returnPopupEvent)
   {
     Object value = returnPopupEvent.getReturnValue();
     if (value != null)
     {
       FileData rowData = _getRowData(value);
       if(rowData != null)
       {
         this.setName(rowData.getName()); 
       }
       else
         this.setName(value.toString());
     }
   }
   
   public void inputReturnPopupListener(ReturnPopupEvent returnPopupEvent)
   {
     Object value = returnPopupEvent.getReturnValue();
     this.setName(value.toString());
   }

   private void _addToRecentValuesList(FileData rowData, List recentValues)
   {
     if (!recentValues.contains(rowData))
       recentValues.add(0, rowData);

     int size = recentValues.size();
     if (size > 3)
       recentValues.remove(3);
   }
   
   private FileData _getRowData(Object selectedRow)
   {
     if (selectedRow != null && selectedRow instanceof List)
     {
       List listvalue = (List) selectedRow;
       for (int i = 0; i < listvalue.size(); i++)
       {
         Object rowData = listvalue.get(i);
         if (rowData instanceof FileData)
         {
           return ((FileData) rowData);
         }
       }
     }
     else if (selectedRow != null && selectedRow instanceof RowKeySet)
     {
       Iterator selection = ((RowKeySet) selectedRow).iterator();
       while (selection.hasNext())
       {
         Object rowKey = selection.next();
         Object oldRowKey = listModel.getRowKey();
         listModel.setRowKey(rowKey);
         FileData rowData = (FileData)listModel.getRowData();
         listModel.setRowKey(oldRowKey);
         return rowData;
       }
     }
     return null;
   }
    
    //验证程序，如果手写填错了会提示没有这个选线
   public void validate(FacesContext facesContext, UIComponent uIComponent, 
                        Object object)
   { 
     for (Object data: _values)
     {
       if (((FileData) data).getName().equals(object))
       {
         return;
       }
     }
     FacesMessage message = 
       new FacesMessage(FacesMessage.SEVERITY_ERROR, getLabel(), 
                        "值不正确");
     throw new ValidatorException(message);
   }

   public List getValues()
   {
      // System.out.println("getValueaaa");
     return _values;
   }

   public List getRecentValues()
   {
     List recentValues = new ArrayList();
     recentValues.addAll(_recentValues);

     if (recentValues.size() > 0)
       recentValues.add(new FileData(null, null));

     return recentValues;
   }
   
//   public List getRecentValues1()
//   {
//     List recentValues = new ArrayList();
//     recentValues.addAll(_recentValues1);
//
//     if (recentValues.size() > 0)
//       recentValues.add(new FileData(null, null));
//
//     return recentValues;
//   }

   // "LaunchPopupListener" attribute EL reachable
//   public LaunchPopupListener getLaunchPopupListener()
//   {
// //    return new DemoLOVPopupListener();
//   }
    
   
   public CollectionModel getListModel()
   {
     return listModel;
   }
 
   public ListOfValuesModel getListOfValuesModel()
   {
     if(_listOfValuesModel == null)
       _listOfValuesModel = new ListOfValuesModelImpl(this, getAttributes());
     return _listOfValuesModel;
   }
   
//   public ListOfValuesModel getListOfValuesModel1()
//   {
//     if(_listOfValuesModel1 == null)
//       _listOfValuesModel1 = new ListOfValuesModelImpl1(this, getAttributes());
//     return _listOfValuesModel1;
//   }
     
   private ListOfValuesModel _listOfValuesModel;
   //private ListOfValuesModel _listOfValuesModel1;
   
    //过滤方法
   private void filterList(String name, List filtered)
   {
      
       
     filtered.clear();
     if (name != null)
     {
       for (Object data: _values)
       {
         if (((FileData) data).getName().contains(name))
         {
           filtered.add(data);
         }
       }
     }
     if (filtered.size() == 0)
     {
       filtered.addAll(_values);
     }
   }

   public void setReturnPopupDataValue1(Object returnPopupDataValue1)
   {
     this.returnPopupDataValue1 = returnPopupDataValue1;
   }

   public Object getReturnPopupDataValue1()
   {
     return returnPopupDataValue1;
   }
 
    public void setAttributes(List<Attribute> _attributes) {
        this._attributes = _attributes;
    }

    public List<Attribute> getAttributes() {
        return _attributes;
    } 

    public void setMapItem(Map<String, Object> mapItem) {
        this.mapItem = mapItem;
    }

    public Map<String, Object> getMapItem() {
        if( mapItem == null)
            mapItem = new HashMap();
        return mapItem;
    }

    // In the real case, LOV is using ListBinding, and the MRU support is inside
   // the datasource in the listBinding.
   class BaseLovCollection
     extends CollectionModel
   {
     public Object getRowKey()
     {
       if (_row != null)
       {
         return _row.getRowId();
       }
       return null;
     }

     /**
      * Finds the row with the matching key and makes it current
      * @param rowKey the rowKey, previously obtained from {@link #getRowKey}.
      */
     public void setRowKey(Object rowKey)
     {
       if (rowKey == null)
       {
         _row = null;
         return;
       }

       int index = -1;
       for (int i = 0; i < _filteredList.size(); i++)
       {
         String rowId = ((FileData) _filteredList.get(i)).getRowId();
         if (rowId.equals(rowKey))
         {
           index = i;
           break;
         }
       }

       setRowIndex(index);
     }

     public void setRowIndex(int rowIndex)
     {
       int size = _filteredList.size();
       if (rowIndex < 0 || rowIndex > size || size == 0)
       {
         _row = null;
         _rowIndex = -1;
       }
       else
       {
         _row = (FileData) _filteredList.get(rowIndex);
         _rowIndex = rowIndex;
       }
     }

     public int getRowIndex()
     {
       return _rowIndex;
     }

     public Object getRowData()
     {
       return _row;
     }

     public int getRowCount()
     {
       return _filteredList.size();
     }

     public boolean isRowAvailable()
     {
       return (_row != null);
     }

     public Object getRowData(int rowIndex)
     {
       int oldIndex = getRowIndex();
       try
       {
         setRowIndex(rowIndex);
         return getRowData();
       }
       finally
       {
         setRowIndex(oldIndex);
       }
     }

     public boolean isSortable(String property)
     {
       return false;
     }

     public List getSortCriteria()
     {
       return Collections.EMPTY_LIST;
     }

     public Object getWrappedData()
     {
       return BaseLovCollection.this;
     }

     public void setWrappedData(Object data)
     {
       throw new UnsupportedOperationException();
     }

     public BaseLovCollection()
     {
     }

     FileData _row = null;
     int _rowIndex = -1;
   }

   class ListLovCollection
     extends CollectionModel
   {
     public Object getRowKey()
     {
       if (_row != null)
       {
         return _row.getRowId();
       }
       return null;
     }

     /**
      * Finds the row with the matching key and makes it current
      * @param rowKey the rowKey, previously obtained from {@link #getRowKey}.
      */
     public void setRowKey(Object rowKey)
     {
       if (rowKey == null)
       {
         _row = null;
         return;
       }

       int index = -1;
       for (int i = 0; i < _filteredList.size(); i++)
       {
         String rowId = ((FileData) _filteredList.get(i)).getRowId();
         if (rowId.equals(rowKey))
         {
           index = i;
           break;
         }
       }

       setRowIndex(index);
     }

     public void setRowIndex(int rowIndex)
     {
       int size = _filteredList.size();
       if (rowIndex < 0 || rowIndex > size || size == 0)
       {
         _row = null;
         _rowIndex = -1;
       }
       else
       {
         _row = (FileData) _filteredList.get(rowIndex);
         _rowIndex = rowIndex;
       }
     }

     public int getRowIndex()
     {
       return _rowIndex;
     }

     public Object getRowData()
     {
       return _row;
     }

     public int getRowCount()
     {
       return _filteredList.size();
     }

     public boolean isRowAvailable()
     {
       return (_row != null);
     }

     public Object getRowData(int rowIndex)
     {
       int oldIndex = getRowIndex();
       try
       {
         setRowIndex(rowIndex);
         return getRowData();
       }
       finally
       {
         setRowIndex(oldIndex);
       }
     }

     public boolean isSortable(String property)
     {
       return false;
     }

     public List getSortCriteria()
     {
       return Collections.EMPTY_LIST;
     }

     public Object getWrappedData()
     {
       return ListLovCollection.this;
     }

     public void setWrappedData(Object data)
     {
       throw new UnsupportedOperationException();
     }

     FileData _row = null;
     int _rowIndex = -1;
   }
 
   public static class ListOfValuesModelImpl
     extends ListOfValuesModel
   {
     private List<Attribute> _attributes;
     private TableModel tableModel;
     
     public ListOfValuesModelImpl(ComboboxLOVBean bean, List<Attribute> attr)
     {
       _bean = bean;
       _attributes = attr;
      
     }

     /**
      * Not applicable as items are only supported in comboLOV
      * @return
      */
     @Override
     public List<? extends Object> getItems()
     {
       return _bean.getValues();
     }

     /**
      * Returns null for now.
      * @return
      */
     @Override
     public QueryModel getQueryModel()
     {
       //  return new QueryModelImpl( _bean);
          return null;
     }

     /**
      * @return
      */
     @Override
     public List<? extends Object> getRecentItems()
     {
        return _bean.getRecentValues(); 
     }

     @Override
     public TableModel getTableModel()
     {
         if(tableModel == null)
              tableModel = new TableModelImpl(_bean.getListModel(), _attributes);
         return tableModel;
     }
     
     @Override
     public List<ColumnDescriptor> getItemDescriptors()
     {
       List<ColumnDescriptor> descriptors = getTableModel().getColumnDescriptors();
       if (descriptors != null && descriptors.size() > 4)
       {
         return descriptors.subList(0,4);
       }
       return descriptors;
     }

     @Override
     public boolean isAutoCompleteEnabled()
     {
       return false;
     }
        //query的方法是在这里过滤的
     public void performQuery(QueryDescriptor qd)
     {  
       AttributeCriterion criterion = (AttributeCriterion) qd.getConjunctionCriterion().getCriterionList().get(0);
       String ename = (String) criterion.getValues().get(0);
       _bean.filterList(ename, _bean._filteredList);
 
     }

     public List<Object> autoCompleteValue(Object value)
     {
       // wierd way of filtering and accessing _filteredList but for now its ok
       _bean.filterList((String) value, _bean._filteredList);
       if (_bean._filteredList.size() == 1)
       {
         List<Object> returnList = new ArrayList<Object>();
         FileData rowData = (FileData) _bean._filteredList.get(0);
         Object rowKey = rowData.getRowId();
         RowKeySet rowKeySet = new RowKeySetImpl();
         rowKeySet.add(rowKey);
         returnList.add(rowKeySet);
         return returnList;
       }
       return null;
     }

     public void valueSelected(Object value)
     {
       FileData rowData = _getRowData(value);
       if(rowData != null)
       {
         _bean.setName(rowData);
         _bean._addToRecentValuesList(rowData, _bean._recentValues);
       }
     }
     
     @Override
     public Object getValueFromSelection(Object selectedRow)
     { 
        
       FileData rowData = _getRowData(selectedRow);
       if(rowData != null)
       {
         return rowData.getName();
       }
         return null;
     }
     
     private FileData _getRowData(Object selectedRow)
     {
       if (selectedRow != null && selectedRow instanceof List)
       {
         List listvalue = (List) selectedRow;
         for (int i = 0; i < listvalue.size(); i++)
         {
           Object rowData = listvalue.get(i);
           if (rowData instanceof FileData)
           {
             return ((FileData) rowData);
           }
         }
       }
       else if (selectedRow != null && selectedRow instanceof RowKeySet)
       {
         Iterator selection = ((RowKeySet) selectedRow).iterator();
         while (selection.hasNext())
         {
           Object rowKey = selection.next();
           Object oldRowKey = _bean.listModel.getRowKey();
           _bean.listModel.setRowKey(rowKey);
           FileData rowData = (FileData)_bean.listModel.getRowData();
           _bean.listModel.setRowKey(oldRowKey);
           return rowData;
         }
       }
       return null;
     }

     public QueryDescriptor getQueryDescriptor()
     { 
       //return _queryDescriptor;
        return null;
     }
     
     private QueryDescriptor _queryDescriptor;
     private ComboboxLOVBean _bean;

   }

   /* LOVModel for comboLOV with LaunchPopupListener */
//   public static class ListOfValuesModelImpl1
//     extends ListOfValuesModel
//   {
//     
//     private List<Attribute> _attributes;
//       
//     public ListOfValuesModelImpl1(ComboboxLOVBean bean, List<Attribute> attr)
//     {
//       _bean = bean;
//         _attributes = attr;
//     }
//
//     /**
//      * Not applicable as items are only supported in comboLOV
//      * @return
//      */
//     @Override
//     public List<? extends Object> getItems()
//     {
//      // return _bean._listenersFilteredList;
//         return null;
//     }
//     
//     /**
//      * Returns null for now.
//      * @return
//      */
//     @Override
//     public QueryModel getQueryModel()
//     {
//      // return new QueryModelImpl(_bean);
//         return null;
//     }
//
//     /**
//      * @return
//      */
//     @Override
//     public List<? extends Object> getRecentItems()
//     {
//       return _bean.getRecentValues1();
//     }
//
//     @Override
//     public TableModel getTableModel()
//     {
//       //return new TableModelImpl(_bean.getListModel(), _attributes);
//         return null;
//     }
//
//     @Override
//     public boolean isAutoCompleteEnabled()
//     {
//       return false;
//     }
//
//     public void performQuery(QueryDescriptor qd)
//     {
//       AttributeCriterion criterion = (AttributeCriterion) qd.getConjunctionCriterion().getCriterionList().get(0);
//       String ename = (String) criterion.getValues().get(0);
//       _bean.filterList(ename, _bean._filteredList);
//     }
//
//     public List<Object> autoCompleteValue(Object value)
//     {
//       // wierd way of filtering and accessing _filteredList but for now its ok
//       _bean.filterList((String) value, _bean._filteredList);
//       if (_bean._filteredList.size() == 1)
//       {
//         List<Object> returnList = new ArrayList<Object>();
//         FileData rowData = (FileData) _bean._filteredList.get(0);
//         Object rowKey = rowData.getRowId();
//         RowKeySet rowKeySet = new RowKeySetImpl();
//         rowKeySet.add(rowKey);
//         returnList.add(rowKeySet);
//         return returnList;
//       }
//       return null;
//     }
//
//     public void valueSelected(Object value)
//     {
//       FileData rowData = _getRowData(value);
//       if(rowData != null)
//       {
//         _bean.setName1(rowData.getName());
//         _bean._addToRecentValuesList(rowData, _bean._recentValues1);
//       }
//     }
//     
//     @Override
//     public Object getValueFromSelection(Object selectedRow)
//     {
//       FileData rowData = _getRowData(selectedRow);
//       if(rowData != null)
//       {
//         return rowData.getName();
//       }
//       return null;
//     }
//     
//     private FileData _getRowData(Object selectedRow)
//     {
//       if (selectedRow != null && selectedRow instanceof List)
//       {
//         List listvalue = (List) selectedRow;
//         for (int i = 0; i < listvalue.size(); i++)
//         {
//           Object rowData = listvalue.get(i);
//           if (rowData instanceof FileData)
//           {
//             return ((FileData) rowData);
//           }
//         }
//       }
//       else if (selectedRow != null && selectedRow instanceof RowKeySet)
//       {
//         Iterator selection = ((RowKeySet) selectedRow).iterator();
//         while (selection.hasNext())
//         {
//           Object rowKey = selection.next();
//           Object oldRowKey = _bean.listModel.getRowKey();
//           _bean.listModel.setRowKey(rowKey);
//           FileData rowData = (FileData)_bean.listModel.getRowData();
//           _bean.listModel.setRowKey(oldRowKey);
//           return rowData;
//         }
//       }
//       return null;
//     }
//
//     public QueryDescriptor getQueryDescriptor()
//     {
//       if(_queryDescriptor == null)
//         _queryDescriptor = new QueryDescriptorImpl(_attributes);
//       return _queryDescriptor;
//     }
//     
//     private QueryDescriptor _queryDescriptor;
//     private ComboboxLOVBean _bean;
//
//   }
//   

   // For now return a void implementation for the querymodel to show a simple query component
   // such that the Search... link will also be displayed in the dropdown
   public static class QueryModelImpl
     extends QueryModel
   {
      ComboboxLOVBean bean;
      
      public QueryModelImpl(ComboboxLOVBean bean) {
            this.bean = bean;
       }
    
     public QueryDescriptor create(String name, QueryDescriptor qdBase)
     { 
       return null;
     }

     public void delete(QueryDescriptor qd)
     {  
     }

     public List<AttributeDescriptor> getAttributes()
     {
       return null;
     }

     public List<QueryDescriptor> getSystemQueries()
     {  
       return null;
     }

     public List<QueryDescriptor> getUserQueries()
     {  
       return null;
     }

     public void reset(QueryDescriptor qd)
     {  
        
     }

     public void setCurrentDescriptor(QueryDescriptor qd)
     { 
     }

     public void update(QueryDescriptor qd, Map<String, Object> uiHints)
     {
          
     }
   }

   // Simple implementation of the QueryDescriptor classs to display one inputText
   // field to filter the data in the table inside dialog based on the Ename
   public static class QueryDescriptorImpl
     extends QueryDescriptor
   { 
     public QueryDescriptorImpl(List<Attribute> attrLabel)
     { 
       _conjCriterion = new ConjunctionCriterionImpl(attrLabel);
     }

     public void addCriterion(String name)
     {      
     }

     public void changeMode(QueryDescriptor.QueryMode mode)
     {
     }

     public ConjunctionCriterion getConjunctionCriterion()
     {
       return _conjCriterion;
     }
     
     public void setConjunctionCriterion(ConjunctionCriterion criterion)
     {
       _conjCriterion = criterion;
     }

     public String getName()
     {
       return null;
     }

     public Map<String, Object> getUIHints()
     {
       return new HashMap<String, Object>();
     }

     public void removeCriterion(oracle.adf.view.rich.model.Criterion object)
     {
     }

     public AttributeCriterion getCurrentCriterion()
     {
       return null;
     }

     public void setCurrentCriterion(AttributeCriterion attrCriterion)
     {
     }
     
     ConjunctionCriterion _conjCriterion;
   }

   public static class AttributeDescriptorImpl
     extends AttributeDescriptor
   {
     private List<Attribute> attrLabel;
     
     public AttributeDescriptorImpl(List<Attribute> attrLabel) {
         this.attrLabel = attrLabel;
     }
     
     public AttributeDescriptor.ComponentType getComponentType()
     {
       return AttributeDescriptor.ComponentType.inputText;
     }

     public String getDescription()
     {
       return null;
     }

     public String getFormat()
     {
       return null;
     }

     public String getLabel()
     {
       return attrLabel.get(0).getLabel();
     }

     public int getLength()
     {
       return 0;
     }

     public int getMaximumLength()
     {
       return 0;
     }

     public Object getModel()
     {
       return null;
     }

     public String getName()
     {
       return null;
     }

     public Set<AttributeDescriptor.Operator> getSupportedOperators()
     {
       return null;
     }

     public Class getType()
     {
       return null;
     }

     public boolean isReadOnly()
     {
       return false;
     }

     public boolean isRequired()
     {
       return false;
     }
   }

   public static class ConjunctionCriterionImpl
     extends ConjunctionCriterion
   {
    private List<Attribute> attrLabel;
    
     public ConjunctionCriterionImpl(List<Attribute> attrLabel)
     {
       this.attrLabel = attrLabel;
       _criterionList = new ArrayList<Criterion>();
       _criterionList.add(new AttributeCriterionImpl(attrLabel));
     }

     public ConjunctionCriterion.Conjunction getConjunction()
     {
       return ConjunctionCriterion.Conjunction.NONE;
     }

     public List<oracle.adf.view.rich.model.Criterion> getCriterionList()
     {
       return _criterionList;
     }

     public Object getKey(oracle.adf.view.rich.model.Criterion criterion)
     {
       return Integer.toString(0);
     }

     public Criterion getCriterion(Object key)
     {
       assert(_criterionList != null);
       return _criterionList.get(0);
     }

     public void setConjunction(ConjunctionCriterion.Conjunction conjunction)
     {
     }
     List<Criterion> _criterionList;
   }
   
   public static class AttributeCriterionImpl extends AttributeCriterion
   {
    private List<Attribute> attrLabel;
    
     public AttributeCriterionImpl(List<Attribute> attrLabel)
     {
       
       this.attrLabel = attrLabel;
       
       if(_values == null)
       {
         _values = new ArrayList<Object>();//可以设置搜索框的值
        // _values.add("A");
       }
     }

     public AttributeDescriptor getAttribute()
     {
       return new AttributeDescriptorImpl(attrLabel);
     }

     public AttributeDescriptor.Operator getOperator()
     {
       return null;
     }

     public Map<String, AttributeDescriptor.Operator> getOperators()
     {
       return null;
     }

     public List<? extends Object> getValues()
     {
       return _values;
     }
     
     public boolean isRemovable()
     {
       return false;
     }

     public void setOperator(AttributeDescriptor.Operator operator)
     {
     }
     
     List<Object> _values;
   }
   
     //按一下后下拉框显示的列的模型
   public static class TableModelImpl
     extends TableModel
   {
     private List<Attribute> _attributes;
     
     public TableModelImpl(CollectionModel collectionModel, List<Attribute> attr)
     {
       assert (collectionModel != null);
       _collectionModel = collectionModel;
         _attributes = attr;
     }
     @Override
     public CollectionModel getCollectionModel()
     {
       return _collectionModel;
     }
    
    
     @Override
     public List<ColumnDescriptor> getColumnDescriptors()
     {
       if(_attributes == null) return new ArrayList<ColumnDescriptor>();
      
       if (_descriptors == null)
       {
         _descriptors = new ArrayList<ColumnDescriptor>(_attributes.size());
         for (Attribute attr: _attributes)
         {//按一下后下拉框显示的列
           _descriptors.add(new ColumnDescriptorImpl(attr));
         }
       }
       return _descriptors;
     }

     public static class ColumnDescriptorImpl
       extends ColumnDescriptor
     {
       public ColumnDescriptorImpl(Attribute name)
       {
         _name = name;
       }

       @Override
       public String getFormat()
       {
         return null;
       }
        //表头列名设置
       @Override
       public String getLabel()
       {
         return _name.getLabel();
       }

       @Override
       public String getName()
       {
         return _name.getValue();
       }

       @Override
       public Class getType()
       {
         return String.class;
       }

       @Override
       public String getAlign()
       {
         return null;
       }

       @Override
       public AttributeDescriptor.ComponentType getComponentType()
       {
         return AttributeDescriptor.ComponentType.inputText;
       }

       @Override
       public String getDescription()
       {
         return null;
       }

       @Override
       public Set<AttributeDescriptor.Operator> getSupportedOperators()
       {
         return Collections.emptySet();
       }

       @Override
       public int getLength()
       {
         return 0;
       }

       public int getMaximumLength()
       {
         return 0;
       }
       public Object getModel()
       {
         return null;
       }
       @Override
       public int getWidth()
       {
         
         return _name.value.length() + 200;
       }

       /**
        * The column attributes are all readOnly.
        *
        * @return
        */
       @Override
       public boolean isReadOnly()
       {
         return true;
       }

       @Override
       public boolean isRequired()
       {
         return false;
       }

       private Attribute _name;

     }


     private CollectionModel _collectionModel;
     private static List<ColumnDescriptor> _descriptors;
   }

 

   public class FileData
   {
     private String name;  
     private String rowId;
    
     
     FileData(String rowId, String name)
     { 
        this.rowId = rowId;
        this.name = name;
     }

     public String getRowId()
     {
       return rowId;
     }
 

        public void setRowId(String rowId) {
            this.rowId = rowId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {

            return name;
        } 
    }

//   public class DemoLOVPopupListener implements LaunchPopupListener
//   {
//     public void processLaunch(LaunchPopupEvent event)
//     {
//       if(LaunchPopupEvent.PopupType.DROPDOWN_LIST.equals(event.getPopupType()))
//       {
//         //DropDown list is launched
//         RichInputComboboxListOfValues comboLOV = (RichInputComboboxListOfValues) event.getComponent();
//         ListOfValuesModelImpl1 lovModel = (ListOfValuesModelImpl1)comboLOV.getModel();
//         String value= (String) event.getSubmittedValue();
//         
//         //Here we filter the values in in dropdownlist acording to the value typed in
//         lovModel._bean.filterList(value, lovModel._bean._listenersFilteredList);
//       }
//     }
//   }
   
   List _values = new ArrayList();
   List _recentValues = new ArrayList();
   List _recentValues1 = new ArrayList();
   List _filteredList = new ArrayList(); //要显示的数据
   List _listenersFilteredList = new ArrayList();

   //listModel is for the table binding for table in LOV popup dialog
   private CollectionModel listModel = new ListLovCollection();
   //baseModel is for defined for the base component.
   private CollectionModel baseModel = new BaseLovCollection();
   //display attributes. 

   private List<Attribute> _attributes = new ArrayList<Attribute>();
   
   public static class Attribute{
       
        String label;
        String value;
        public Attribute(String label, String value) {
            this.label = label;
            this.value = value;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
  
   private Map<String,Object> mapItem;  //用户映射外键
     
   public List suggestMethod(FacesContext facesContext,
                               AutoSuggestUIHints autoSuggestUIHints) {
         
          
         String key = autoSuggestUIHints.getSubmittedValue();   
         List<SelectItem> res = new ArrayList<SelectItem>();
         
         for(int i = _values.size()-1; i>=0; i-- ) {
             
             FileData data = (FileData)_values.get(i);
             if( data.getName().toUpperCase().contains(key.toUpperCase())) { 
                 SelectItem item = new SelectItem();
                 item.setValue(data.getName());
                 item.setLabel(data.getName());
                 res.add(item);
             }
         }
         return res;
     }
    
     //根据绑定的名字的items获取ComboboxBean
      static public ComboboxLOVBean createByBinding(String name) {
      
          //System.out.println(bindings.get("Names"));
          List<SelectItem> data = (List)JSFUtils.resolveExpression("#{bindings." + name +".items}");
          
          String label = (String)JSFUtils.resolveExpression("#{bindings." + name + ".label}");
          
          List<ComboboxLOVBean.Attribute> attrs = new ArrayList<ComboboxLOVBean.Attribute>();
          attrs.add(new ComboboxLOVBean.Attribute(label ,"name"));
      //        for(SelectItem item : data) {
      //            System.out.println(item.getLabel()+"   "+item.getValue());
      //        }
          ComboboxLOVBean comboboxBean = new ComboboxLOVBean(data, attrs);
      
          if(data == null) 
          {//System.out.println("!!!!!!!!  "+name);
             return null;}
          for(int i = 0; i < data.size(); i++)
              comboboxBean.getMapItem().put(data.get(i).getLabel(), i);
          
          return comboboxBean;
      }  
}
