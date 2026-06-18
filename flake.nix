{
  description = "Kotlin MC modding dev environment: pinned JDK, Gradle, Kotlin, and IntelliJ with plugins and native MC libs for debugging";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    nix-jetbrains-plugins.url = "github:theCapypara/nix-jetbrains-plugins";
    nix-jetbrains-plugins.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs = {
    self,
    nixpkgs,
    nix-jetbrains-plugins,
  }: let
    # gradle, kotlin, and the IDE all build against this JDK version
    javaVersion = 21;

    systems = [
      "x86_64-linux"
      "aarch64-linux"
    ];

    forAllSystems = f:
      nixpkgs.lib.genAttrs systems (
        system: let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfree = true;
          };

          jdk = pkgs."jdk${toString javaVersion}";
          gradle = pkgs.gradle.override {java = jdk;};
          kotlin = pkgs.kotlin.override {jre = jdk;};

          # Native libraries Minecraft loads at runtime. Mojang's game files
          # are prebuilt for a normal Linux layout and look for these on a
          # global path that NixOS does not have.
          minecraftLibs = with pkgs; [
            # Graphics
            libGL
            glfw
            # Audio
            openal
            alsa-lib
            libpulseaudio
            pipewire
            libjack2
            # X11 (also used under XWayland)
            libX11
            libXcursor
            libXext
            libXi
            libXrandr
            libXxf86vm
            # Misc runtime
            udev
            vulkan-loader
            flite
            stdenv.cc.cc.lib
          ];

          # IntelliJ with marketplace plugins pinned by the flake
          ideaWithPlugins = nix-jetbrains-plugins.lib.buildIdeWithPlugins pkgs "idea" [
            "com.demonwav.minecraft-dev"
          ];

          # Wrap IntelliJ with the native libs so runClient and everything it
          # spawns (the Gradle daemon, the runClient JVM) find them.
          ideaWrapped = pkgs.symlinkJoin {
            name = "${ideaWithPlugins.name}-mc";
            paths = [ideaWithPlugins];
            nativeBuildInputs = [pkgs.makeWrapper];
            postBuild = ''
              shopt -s nullglob
              libPath="${pkgs.lib.makeLibraryPath minecraftLibs}"
              for prog in "$out"/bin/*; do
                wrapProgram "$prog" --prefix LD_LIBRARY_PATH : "$libPath"
              done
              # Repoint launcher entries at the wrapped binaries.
              for desktop in "$out"/share/applications/*.desktop; do
                if [ -L "$desktop" ]; then
                  target=$(readlink -f "$desktop")
                  rm "$desktop"
                  sed "s#${ideaWithPlugins}#$out#g" "$target" > "$desktop"
                fi
              done
            '';
          };
        in
          f {inherit pkgs jdk gradle kotlin ideaWrapped;}
      );
  in {
    devShells = forAllSystems (
      {
        pkgs,
        jdk,
        gradle,
        kotlin,
        ideaWrapped,
      }: {
        default = pkgs.mkShell {
          packages = [
            jdk
            gradle
            kotlin
            ideaWrapped
          ];

          # Point command line tools at the pinned JDK.
          JAVA_HOME = "${jdk.home}";

          # Expose the JDK at a stable path so IntelliJ can point at it
          # without breaking on every store rebuild.
          shellHook = ''
            mkdir -p .idea-env
            ln -sfn "${jdk.home}" .idea-env/jdk
          '';
        };
      }
    );

    # `nix run` runs the project's Gradle wrapper. This is the flake native
    # equivalent of a build alias. Inside the dev shell you can also just
    # run `gradle build` or `./gradlew build` directly.
    apps = forAllSystems (
      {
        pkgs,
        jdk,
        ...
      }: let
        build = pkgs.writeShellScript "build" ''
          export JAVA_HOME="${jdk.home}"
          exec ./gradlew build "$@"
        '';
      in {
        default = {
          type = "app";
          program = "${build}";
        };
      }
    );

    formatter = forAllSystems ({pkgs, ...}: pkgs.nixfmt);
  };
}
