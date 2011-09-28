class leiningen() {
  file { "/home/vagrant/bin/lein":
      source  => "puppet:///modules/leiningen/lein.sh",
      owner   => "vagrant",
      group   => "vagrant",
      mode    => "755",
      require => File["/home/vagrant/bin"],
  }
}
