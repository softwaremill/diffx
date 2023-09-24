{
  description = "Python shell flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";

    mach-nix.url = "github:davhau/mach-nix";
  };

  outputs = { self, nixpkgs, mach-nix, flake-utils, ... }:
    let
      pythonVersion = "python37";
    in
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        mach = mach-nix.lib.${system};

        pythonEnv = mach.mkPython {
          python = pythonVersion;
          requirements = builtins.readFile ./requirements.txt;
        };
        watchDocs =
          let
            name = "watchDocs";
            src = pkgs.writeShellScript name ''
              sphinx-autobuild . _build/html
            '';
          in
          pkgs.stdenv.mkDerivation
            {
              inherit name src;

              phases = [ "installPhase" "patchPhase" ];

              installPhase = ''
                mkdir -p $out/bin
                cp $src $out/bin/${name}
                chmod +x $out/bin/${name}
              '';
            };
      in
      {
        devShells.default = pkgs.mkShellNoCC {
          packages = [ pythonEnv watchDocs ];

          shellHook = ''
            export PYTHONPATH="${pythonEnv}/bin/python"
          '';
        };
      }
    );
}
