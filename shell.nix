{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [
    pkgs.jdk21
  ];

  shellHook = ''
    echo "Java environment loaded"
    java -version
  '';
}
