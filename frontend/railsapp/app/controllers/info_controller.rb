java_import 'de.fusionfactory.index_vivus.configuration.Environment'

class InfoController < ApplicationController
  
  def get_classpath
      render :json => $CLASSPATH.to_a
  end
  
  def get_loadpath
      render :json => $LOADPATH.to_a     
  end

  def get_infos
      render :json => {rails_env: Rails.env, 
            backend_env: Environment.get_active.name,
            classpath: $CLASSPATH.to_a, 
            loadpath: $LOADPATH.to_a}
  end
end