package com.ayosec.misc

object POSIX {

  // Remove the directory and its contents recursively
  def delete(path: java.io.File) {
    if(path.isDirectory) {
      for(file <- path.listFiles) delete(file)
      path.delete
    } else {
      path.delete
    }
  }

}
