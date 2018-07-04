# exfmtconv

Converter to convert OpenStreetMap XML (.osm) files to the custom ExchangeFormat we use. This converter supports clipping and reduction of lanes to one per direction. The full list of arguments can be seen by specifying `-h` or `--help`:

```shell
usage: exfmtconv
 -c,--clip <MINLAT;MINLON;MAXLAT;MAXLON>   Clip to specified bounds
 -h,--help                                 Print this message
 -i,--input <IN_FILE>                      Input file
 -m,--multilane <MULTILANE>                Enable or disable multi-lane
                                           output (defaults to true)
 -o,--output <OUT_FILE>                    Output file
```

Note, that when using this via gradle, you have to specify the arguments using `-Dexec.args="<actual arguments>"`, i.e. to convert the file `baden-wuerttemberg.osm` to `tuebingen-stuttgart-singlelane.mtsmap` using clipping and lane-reduction, you would call:

```shell
exfmtconv \
 -i baden-wuerttemberg.osm \
 -o tuebingen-stuttgart-singlelane.mtsmap \
 -c 48.3489;8.7369;48.8656;9.4338 \
 -m false
```

or, using gradle in the root-directory of the project:

```
gradle :tools:exfmtconv:run -Dexec.args=" \
 -i baden-wuerttemberg.osm \
 -o tuebingen-stuttgart-singlelane.mtsmap \
 -c 48.3489;8.7369;48.8656;9.4338 \
 -m false"
```
