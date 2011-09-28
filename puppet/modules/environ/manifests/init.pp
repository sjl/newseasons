class environ() {
  file { "/home/vagrant/.bashrc":
      source  => "puppet:///modules/environ/bashrc",
      owner   => "vagrant",
      group   => "vagrant",
      mode    => "755",
      require => User["vagrant"],
  }
}
