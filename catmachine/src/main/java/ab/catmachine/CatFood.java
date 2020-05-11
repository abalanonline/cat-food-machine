/*
 * Copyright 2020 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ab.catmachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatFood implements Serializable {
  private final static String[] FOOD_TYPES = {
      "something", "salmon", "tuna", "mutton", "chicken", "turkey", "duck", "goose",
      "beef", "pork", "eggs", "ham", "liver", "giblets", "crab", "whitefish"};

  private final static String[] FOOD_TEXTURES = {
      "inedible", "pate", "shredded", "cubed", "flaked", "minced", "morsels", "sliced",
      "stewed", "blended", "liquid", "smooth", "mixed", "chopped", "chunked", "solid"};

  private UUID uuid;

  public int getTexture() {
    return (int) (uuid.getMostSignificantBits() & 0x0F);
  }

  public int getFoodType() {
    return (int) (uuid.getLeastSignificantBits() & 0x0F);
  }

  @Override
  public String toString() {
    return FOOD_TEXTURES[getTexture()] + " " + FOOD_TYPES[getFoodType()] + " " + uuid.toString().substring(34);
  }

}
