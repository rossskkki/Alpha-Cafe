import request from '@/utils/request'

//上传文件
export const uploadFileAPI = (file: File) => {
    const formData = new FormData()
    formData.append('file', file) // 这里的'file'参数名必须与后端的MultipartFile参数名一致
    
    return request({
      url: 'common/upload', // 根据你的实际后端接口路径调整
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data' // 对于文件上传，必须使用这个Content-Type
      }
    })
}