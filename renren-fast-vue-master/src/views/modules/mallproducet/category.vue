<template>
<!--  页面标签-->
  <div>
    <el-switch
      v-model="draggable"
      active-text="开启拖拽功能"
      inactive-text="关闭拖拽功能">
    </el-switch>
    <el-button v-if="draggable" @click="updateSort">批量更新</el-button>
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <el-tree :data="data" :props="defaultProps" :expand-on-click-node="false" show-checkbox node-key="catId"
             :default-expanded-keys=expandKeys :draggable="draggable" :allow-drop="allowDrop" @node-drop="handleDrop" ref="tree">
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button v-if="data.catLevel <= 2" type="text" size="mini" @click="() => append(data)">
            添加
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            修改
          </el-button>
          <el-button v-if="data.childrens.length == 0" type="text" size="mini" @click="() => remove(node, data)">
            删除
          </el-button>
        </span>
      </span>
    </el-tree>
<!--    添加弹窗-->
    <el-dialog
      :title="dialogType ? '新增' : '修改'"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false">
      <el-form :model="categoryForm">
        <el-form-item label="类别名称">
          <el-input v-model="categoryForm.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图表">
          <el-input v-model="categoryForm.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="categoryForm.produdetUnit" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="submitForm">确 定</el-button>
  </span>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable */
export default {
// 方法
  data() {
    return {
      pCids:[],    // 拖拽后父节点数据组
      draggable:false,
      updateNodes: [],
      maxLevel:0,
      dialogType: false,
      dialogVisible: false,
      categoryForm:{name:null,icon:null,producetUnit:null,showStatus:1,sort:0,catId:null,catLevel:1},
      data: [],
      expandKeys: [],
      defaultProps: {
        children: "childrens",
        label: "name"
      },
    };
  },
  methods: {
    getCategory() {
      this.$http({
        url: this.$http.adornUrl('/mallproducet/category/listTree'),
        method: 'get'
      }).then(({data}) => {
        console.log("成功获取类别数据: ",data.data)
        this.data = data.data
      })
    },
    append(data) {
      this.dialogVisible=true; // 打开弹窗
      // console.log("添加",data);
      this.categoryForm.parentCid = data.catId; // 添加类别对应父菜单
      this.categoryForm.catLevel = data.catLevel + 1; // 设置添加类别层级
      this.categoryForm.showStatus = 1;  // 菜单显示状态 1 显示 0 被删除
      this.categoryForm.sort = 0;  //排序 默认为0

      //重置更新的数据
      this.categoryForm.catId = null;
      this.categoryForm.name = "";
      this.categoryForm.icon = "";
      this.producetUnit = "";

      this.dialogType = true;   //更新状态
    },
    addDialog() {
      // 添加三级分类的类别信息
      this.$http({
        url: this.$http.adornUrl('/mallproducet/category/save'),
        method: 'post',
        data: this.$http.adornData(this.categoryForm, false)
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message({
            message: '操作成功',
            type: 'success',
          });
          this.dialogVisible = false;
          // 刷新页面数据
          this.getCategory();
          // 设置默认展开的父节点的信息
          this.expandKeys=[this.categoryForm.parentCid]
        } else {
          this.$message.error(data.msg)
        }
      });
    },
    editDialog() {
      // 更新三级目录
      this.$http({
        url: this.$http.adornUrl('/mallproducet/category/update'),
        method: 'post',
        data: this.$http.adornData(this.categoryForm, false)
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message({
            message: '操作成功',
            type: 'success',
          });
          this.dialogVisible = false;
          // 刷新页面数据
          this.getCategory();
          // 设置默认展开的父节点的信息
          this.expandKeys=[this.categoryForm.parentCid]
        } else {
          this.$message.error(data.msg)
        }
      });
    },
    edit(data) {
      this.dialogType = false;   //更新状态
      // 获取最新数据回写
      this.$http({
        url: this.$http.adornUrl(`/mallproducet/category/info/${data.catId}`),
        method: 'post',
        data: this.$http.adornData(this.categoryForm, false)
      }).then(({data}) => {
        this.categoryForm.name = data.category.name;
        this.categoryForm.producetUnit = data.category.producetUnit;
        this.categoryForm.icon = data.category.icon;
        this.categoryForm.catLevel = data.category.catLevel;
        this.categoryForm.parentCid = data.category.parentCid;
        //填充更新数据id
        this.categoryForm.catId = data.category.catId;
        this.categoryForm.showStatus = 1;
        this.categoryForm.sort = 0;

        this.dialogVisible=true;
      });
    },
    batchDelete() {
      // 批量删除的方法
      let catIds = [];
      let checkedNodes = this.$refs.tree.getCheckedNodes(false,false);
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId);
      }
      this.$confirm('是否确认删除该记录?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {

        // 提交到后台服务
        this.$http({
          url: this.$http.adornUrl('/mallproducet/category/delete'),
          method: 'post',
          data: this.$http.adornData(catIds, false)
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.$message({
              message: '操作成功',
              type: 'success',
            });
            // 刷新页面数据
            this.getCategory();
            // 设置默认展开的父节点的信息
            //this.expandKeys = this.pCids;
          } else {
            this.$message.error(data.msg)
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    handleDrop(dragginNode, dropNode,type) {
      //1. 拖拽节点的父节点修改
      let  parentId = 0;
      let siblings = null;
      if (type == 'inner') {
        parentId = dropNode.data.catId;
        // 查找拖拽节点的所有兄弟节点
        siblings = dropNode.childNodes;
      }else {
        parentId = dropNode.parent.data.catId;
        siblings = dropNode.parent.childNodes;
      }
      //2. 拖拽后节点的排序问题
      for (let i = 0; i < siblings.length; i++) {
        if (siblings[i].data.catId == dragginNode.data.catId) {
          // 获取拖拽节点，更新节点 父节点
          //3. 拖拽后的节点及其他节点的深度更新问题
          let catLevel = dragginNode.level;
          if (siblings[i].level != dragginNode.level) {
            // 拖拽后节点层级变化
            catLevel = siblings[i].level;
            //递归更新子节点层级
            this.updateChindNodeLevel(siblings[i]);
          }
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: parentId,
            catLevel: catLevel
          });
        }else {
          this.updateNodes.push({catId: siblings[i].data.catId,sort:i});
        }
      }
      //拖拽后数据节点提交后
      //this.updateSort(parentId);
      this.pCids.push(parentId);
    },
    updateSort() {
      this.$http({
        url: this.$http.adornUrl('/mallproducet/category/updateBatch'),
        method: 'post',
        data: this.$http.adornData(this.updateNodes, false)
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.$message({
            message: '数据拖拽操作成功',
            type: 'success',
          });
          // this.dialogVisible = false;  //关闭弹窗
          // 刷新页面数据
          this.getCategory();
          // 设置默认展开的父节点的信息
          this.expandKeys= this.pCids;
          //重置存储拖拽数据的容器
          this.updateNodes=[];
          this.maxLevel = 0;
        } else {
          this.$message.error(data.msg);
        }
      });
    },
    updateChindNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          var childNode = node.childNodes[i].data;
          this.updateNodes.push({catId:childNodes.catId,catLevel:node.childNodes[i].level});
        }
      }
    },
    //type参数有三种情况: prev inner next
    //dragginNode拖拽节点, dropNode 目标节点
    allowDrop(dragginNode, dropNode,type) {
      //1. 获取当前被拖拽节点最大深度
      this.countNodeLevel(dragginNode);
      let  deep = Math.abs(this.maxLevel = dragginNode.level) + 1;
      if (type == 'inner') {
        return deep + dropNode.level <= 3;
      }
      return deep + dropNode.parent.level <= 3;
    },
    countNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level;
          }
          this.countNodeLevel(node.childNodes[i]);
        }
      }
    },
    submitForm() {
      // 判断当前操作
      if (this.dialogType) {
        // 添加操作
        this.addDialog();
      }else {
        // 更新操作
        this.editDialog();
      }
    },
    remove(node, data) {
      this.$confirm('是否确认删除该记录?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 传递的数据
        let  ids = [data.catId];
        // 提交到后台服务
        this.$http({
          url: this.$http.adornUrl('/mallproducet/category/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.$message({
              message: '操作成功',
              type: 'success',
            });
            // 刷新页面数据
            this.getCategory();
            // 设置默认展开的父节点的信息
            this.expandKeys=[node.parent.data.catId]
          } else {
            this.$message.error(data.msg)
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
      //console.log("删除",data,node)
    },
  },created() {
    this.getCategory();
  }
};
</script>

<style scoped>

/*css样式*/
</style>
