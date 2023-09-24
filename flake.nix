{
  inputs.nixpkgs.url = "github:nixos/nixpkgs";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils, ... }@inputs:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            (sbt.override {
              jre = temurin-jre-bin-17;
            })
            nodejs
            yarn
          ];
          welcomeMessage = ''
            Welcome to the Diffx Nix shell! 👋
          '';

          shellHook = ''
            echo "$welcomeMessage"
          '';
        };
      }
    );
}

