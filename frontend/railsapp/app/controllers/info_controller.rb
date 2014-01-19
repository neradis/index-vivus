
class InfoController < ApplicationController
  
  def get_classpath
        render :json => $CLASSPATH.to_a
  end
  
  def get_loadpath
      render :json => $LOADPATH.to_a     
  end
  
end