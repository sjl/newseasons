Vagrant::Config.run do |config|
  config.vm.host_name = "newseasons"
  config.vm.box = "lucid32"
  config.vm.box_url = "http://files.vagrantup.com/lucid32.box"

  #                              guest <-- host
  config.vm.forward_port "http", 8000,     4565

  config.vm.provision :puppet do |puppet|
    puppet.manifest_file  = "base.pp"
    puppet.manifests_path = "puppet"
    puppet.module_path = "puppet/modules"
  end
end
