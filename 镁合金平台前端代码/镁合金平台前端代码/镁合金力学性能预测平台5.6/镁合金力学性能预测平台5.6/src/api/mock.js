import Mock from 'mockjs'
import data from './data'
Mock.mock('/api/getData','get',data.tableData)